FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw

RUN ./mvnw -q -e -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -e -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=builder /workspace/target/*-SNAPSHOT.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=75.0"
ENV SERVER_PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
