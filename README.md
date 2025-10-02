# Challenge – Microservicio de Posts

> **Gracias**
> 
> ¡Muchas gracias al equipo de **Banco Hipotecario** por la oportunidad de participar en este challenge!
> Acá está todo lo necesario para correr el proyecto, probar los endpoints y entender las decisiones técnicas.

---

## 🚀 Levantar todo con Docker (copy-paste)

```bash
# 1) Limpiar 
docker compose down -v --remove-orphans

# 2) Build + up en segundo plano
docker compose up --build -d

# 3) Ver servicios
docker compose ps

# 4) Seguir logs (app + redis)
docker compose logs -f --tail=200 challenge-app challenge-redis
```

* **App**: `http://localhost:8080`
* **Redis**: puerto interno `6379`

> Tip: dejá los logs abiertos mientras probás con Postman/cURL para ver hits a upstream y cache hits.

---

## 🔌 Endpoints

### 1) `GET /posts`

Obtiene los posts **mergeados** con su autor y comentarios. Soporta **paginación** y **ordenamiento** opcionales.

#### Parámetros opcionales

* `page` (Integer, 0-based): default `0`. Si `< 0`, se normaliza a `0`.
* `size` (Integer): default `20`. Acepta `1..100`. Fuera de rango → `20`.
* `sort` (String): `campo[,desc]`. Campos: `postId` (default), `title`, `commentsCount`.
  Ej.: `sort=title,desc` o `sort=commentsCount`

#### Ejemplos cURL

```bash
# Todos (sin paginación; devuelve una lista)
curl -s http://localhost:8080/posts | jq

# Página 0, tamaño 5, orden por título descendente
curl -s "http://localhost:8080/posts?page=0&size=5&sort=title,desc" | jq

# Página 1, tamaño 10, orden por cantidad de comentarios ascendente
curl -s "http://localhost:8080/posts?page=1&size=10&sort=commentsCount" | jq
```

#### Cuerpos de respuesta (JSON)

**Envelope común**

```json
{
  "status": 200,
  "data": {}
}
```

**a) Sin `page/size` → `data` es `List<PostResponseDTO>`**

```json
{
  "status": 200,
  "data": [
    {
      "postId": 1,
      "title": "sunt aut facere repellat provident occaecati",
      "body": "quia et suscipit\nsuscipit recusandae...",
      "author": {
        "id": 1,
        "name": "Leanne Graham",
        "email": "Sincere@april.biz"
      },
      "comments": [
        {
          "id": 1,
          "email": "Eliseo@gardner.biz",
          "body": "laudantium enim quasi est quidem magnam"
        }
      ],
      "commentsCount": 5
    }
  ]
}
```

**b) Con `page` o `size` → `data` es `PageResponse<PostResponseDTO>`**

```json
{
  "status": 200,
  "data": {
    "content": [
      {
        "postId": 1,
        "title": "sunt aut facere repellat provident occaecati",
        "body": "quia et suscipit\nsuscipit recusandae...",
        "author": { "id": 1, "name": "Leanne Graham", "email": "Sincere@april.biz" },
        "comments": [
          { "id": 1, "email": "Eliseo@gardner.biz", "body": "laudantium enim quasi est quidem magnam" }
        ],
        "commentsCount": 5
      }
    ],
    "page": 0,
    "size": 5,
    "totalElements": 100,
    "totalPages": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

### 2) `DELETE /posts/{id}`

Simula el borrado del post en JSONPlaceholder e **invalida la caché** local del listado.

#### Ejemplos cURL

```bash
# Borrado OK (204 No Content)
curl -i -X DELETE http://localhost:8080/posts/1

# Validación (id debe ser > 0) → error
curl -i -X DELETE http://localhost:8080/posts/0
```

**Respuesta exitosa**

```
HTTP/1.1 204 No Content
```

**Respuesta de error estandarizada (ejemplo)**

```json
{
  "status": 400,
  "error": {
    "code": "APP00",
    "message": "Parámetro inválido: id debe ser positivo"
  },
  "timestamp": "2025-10-02T00:00:00Z",
  "path": "/posts/0"
}
```

---

## 🔎 Mapeo de campos (de JSONPlaceholder → DTOs)

| Origen JSONPlaceholder              | Campo destino DTO                  | Detalle / Normalización                                                 |
| ----------------------------------- | ---------------------------------- | ----------------------------------------------------------------------- |
| `/posts` → `id`                     | `PostResponseDTO.postId`           | Orden global por `postId` al final                                      |
| `/posts` → `title`                  | `PostResponseDTO.title`            | `TextSanitizer.text(...)` (trim + colapso espacios; vacío→`null`)       |
| `/posts` → `body`                   | `PostResponseDTO.body`             | `TextSanitizer.text(...)`                                               |
| `/posts` → `userId`                 | (clave para `/users/{id}`)         | Se usa para enriquecer autor                                            |
| `/users/{id}` → `id`                | `PostResponseDTO.author.id`        | Cache de `CompletableFuture` por `userId` para evitar duplicar llamadas |
| `/users/{id}` → `name`              | `PostResponseDTO.author.name`      | `TextSanitizer.text(...)`                                               |
| `/users/{id}` → `email`             | `PostResponseDTO.author.email`     | `TextSanitizer.emailTrim(...)` (+ validación `@Email`)                  |
| `/posts/{id}/comments` → `[].id`    | `PostResponseDTO.comments[].id`    | Ordenados por `id`                                                      |
| `/posts/{id}/comments` → `[].email` | `PostResponseDTO.comments[].email` | `TextSanitizer.emailTrim(...)`                                          |
| `/posts/{id}/comments` → `[].body`  | `PostResponseDTO.comments[].body`  | `TextSanitizer.text(...)`                                               |
| (derivado)                          | `PostResponseDTO.commentsCount`    | `comments.size()`                                                       |

---

## 🧠 Cómo se arma la respuesta (merge + concurrencia)

* Use case: `BuildPostFullInfoImpl#getPosts()`

    1. Trae todos los **posts**.
    2. Para cada post, en **paralelo** (virtual threads):

        * **Usuario** (`/users/{id}`) — con cache local de `CompletableFuture` por `userId`.
        * **Comentarios** (`/posts/{id}/comments`).
    3. Mapea a `PostResponseDTO` (con sanitización y validaciones).
    4. Ordena globalmente por `postId`.

---

## 📑 Paginación y ordenamiento

* `PegeablePostsImpl#page(page, size, sort)`:

    * Aplica orden en memoria por `postId`/`title`/`commentsCount` (asc o desc).
    * Calcula `from`/`to` y devuelve `PageResponse<T>` con `hasNext/hasPrevious`, `totalElements`, `totalPages`.
* Si **no** pasan `page/size`, el controller retorna **lista completa** (y es la que se cachea).

---

## 🧼 Sanitización y validaciones

* **TextSanitizer**

    * `text(s)`: trim + colapsa espacios; si queda vacío → `null`.
    * `emailTrim(s)`: trim + valida `@Email` (Jakarta). Si falla → `IllegalArgumentException`.
* **DTOs**

    * `PostResponseDTO`: `@NotNull`, `@NotBlank`, `@Positive` según campo.
* **Entradas**

    * `@Validated` en controller.
    * `@Positive` en `DELETE /posts/{id}`.
    * `page/size` se normalizan a valores seguros (`0` y `20`).

---

## ⚡ Virtual Threads

* **Propósito**: disparar llamadas IO concurrently (usuarios/comentarios) con muy bajo overhead.
* **Dónde**: `BuildPostFullInfoImpl` recibe un `ExecutorService` de virtual threads e invoca `CompletableFuture.supplyAsync(...)` por cada post.
* **Cuándo**: en cada request de `GET /posts` (fase de “merge”).
* **Beneficio**: excelente escalabilidad para IO-bound (HTTP) sin bloquear el thread del servlet.

---

## 🛡️ Resilience y manejo de fallas

* **Resilience4j** en cada use case que llama a upstream:

    * `@CircuitBreaker(name="jph")`
    * `@Retry(name="jph")`
    * `@Bulkhead(name="jph", type=SEMAPHORE)`
* **Traducción de errores HTTP** (`JsonPlaceholderClientRest#exceptionGuard`):

    * `400` → `BadRequestValidationException`
    * `401` → `UpstreamUnauthorizedException`
    * `403` → `UpstreamForbiddenException`
    * `404` → `ResourceNotFoundException` (con `ErrorCode` específico)
    * `408/504` → `UpstreamTimeoutException`
    * `429` → `UpstreamRateLimitException`
    * `500/502` → `UpstreamServerErrorException`
    * `503` → `UpstreamUnavailableException`
* **Estandarización**:

    * `GlobalExceptionHandler` normaliza payloads de error `{ status, error:{code,message}, timestamp, path }` y loggea con un tag tipo `[APP00]`.

---

## 🧰 Cache (Redis)

* **Cache name**: `posts:all` (constante `POSTS_ALL_CACHE`).
* **Qué cachea**: **solo** la **lista completa** de `PostResponseDTO` retornada por `BuildPostFullInfoImpl#getPosts()` cuando no hay `page/size`.
* **TTL**: 10 minutos.
* **Evicción**: `@CacheEvict(value="posts:all", allEntries=true)` en `DeletePostByIdImpl#execute`.
* **Serialización**:

    * `RedisCacheManager`:

        * Keys: `StringRedisSerializer`.
        * Default values: `GenericJackson2JsonRedisSerializer`.
        * Cache específica `posts:all`: `Jackson2JsonRedisSerializer` **tipado** a `List<PostResponseDTO>` (evita problemas de tipo).

---

## 🏗️ Arquitectura

* **Casos de uso puros** (clean use cases): una responsabilidad, fáciles de testear/mockear.
* **Adaptador HTTP** (`RestClient`): traduce estados HTTP a excepciones de dominio (webhook/adapter).
* **Resilience4j**: circuit breaker, retry, bulkhead → tolerancia a fallos/transitorios.
* **Controller delgado**: valida inputs, orquesta y retorna `DataResponse`.
* **Cache selectivo**: lista completa cacheada; la paginación/orden en memoria evita explosión de keys.

---

## 📋 Checklist vs. Consignas del Challenge

**Objetivo / API externa**

* ✅ Consume **JSONPlaceholder** (`/posts`, `/users/{id}`, `/posts/{id}/comments`).
* ✅ Mergea y retorna datos procesados.


---

### **Endpoints requeridos**

* ✅ `GET /posts`: múltiples llamadas, merge, campos procesados (autor, comentarios, `commentsCount`), sanitización y ordenamiento/paginación.
* ✅ `DELETE /posts/{id}`: llamada DELETE simulada, respuesta adecuada, **evicción de cache**.

### **Requisitos técnicos obligatorios**

* ✅ API REST funcional.
* ✅ Manejo de excepciones y errores HTTP (mapeo a excepciones de dominio + handler global).
* ✅ Logging básico (app + handler + logs de contenedores).
* ✅ README con instrucciones de ejecución (este documento).
* ✅ **Tests unitarios/integ.** (incluidos y pasando).

### **Requisitos valorables**

* ✅ Configuración externalizada (propiedades/yaml + docker compose).
* ✅ Implementación de **cache** (Redis) para optimizar llamadas repetidas (lista completa).
* ✅ Validaciones de entrada y sanitización (`@Validated`, `@Positive`, `@Email`, `TextSanitizer`).
* ✅ Documentación con OpenAPI/Swagger (vía `springdoc-openapi` en el controller).

### **Criterios de evaluación**

* ✅ Funcionalidad (endpoints ok).
* ✅ Integración (múltiples servicios externos).
* ✅ Código / organización (use cases + adapter + config).
* ✅ Manejo de errores / timeouts (Resilience4j + mapping).
* ✅ Performance (concurrencia con **virtual threads** + cache).
* ✅ **Testing** (tests incluidos y verdes).

---

## 🧪 Cómo correr los tests

> Nota: Los tests usan el perfil `test` y deshabilitan Redis/Swagger; **no necesitás levantar Redis** para ejecutarlos.

**Local (Maven Wrapper):**

```bash
./mvnw clean test
# test específico
./mvnw -Dtest=UseCasesCompilationSmokeTest test
```

**Docker (sin instalar Maven/Java localmente):**

```bash
# Ejecuta todos los tests dentro de un contenedor Maven
docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.9-eclipse-temurin-21 \
  mvn -q clean test

# Test específico
docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.9-eclipse-temurin-21 \
  mvn -q -Dtest=UseCasesCompilationSmokeTest test
```

**Docker Compose (opcional, si ya tenés los servicios levantados):**

```bash
# Ver logs de la app/redis (no requerido para tests)
docker compose logs -f --tail=200 challenge-app challenge-redis
```

---

## 🚀 Atajos de prueba manual (cURL)

```bash
# GET sin paginar (construye + cachea)
curl -s http://localhost:8080/posts | jq

# GET sin paginar (cache hit en 10 min si no hay DELETE)
curl -s http://localhost:8080/posts | jq

# GET paginado (no impacta el cache)
curl -s "http://localhost:8080/posts?page=0&size=5&sort=title,desc" | jq

# DELETE (evict del cache de lista completa)
curl -i -X DELETE http://localhost:8080/posts/1

# GET sin paginar (reconstruye y vuelve a cachear)
curl -s http://localhost:8080/posts | jq
```

---

## 📎 Archivos/clases relevantes

* **Controller**: `com.challenge.entrypoint.ChallengeEntrypoint`
* **Use cases**:

  * Merge: `com.challenge.usecase.impl.BuildPostFullInfoImpl`
  * Paginado: `com.challenge.usecase.impl.PegeablePostsImpl`
  * Upstream: `GetAllPostsImpl`, `GetUserByIdImpl`, `GetCommentsByPostImpl`, `DeletePostByIdImpl`
* **Cliente HTTP**: `com.challenge.client.rest.JsonPlaceholderClientRest`
* **Cache**: `com.challenge.config.RedisCacheConfig`
* **DTOs**: `com.challenge.dto.*`, `com.challenge.dto.response.PageResponse`
* **Sanitizer**: `com.challenge.util.impl.TextSanitizerImpl`
* **Errores**: `com.challenge.exception.*`, `com.challenge.exception.error.ErrorCode`
* **Envelope**: `com.challenge.entrypoint.dto.DataResponse`

---
