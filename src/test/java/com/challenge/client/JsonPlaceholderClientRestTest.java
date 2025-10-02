package com.challenge.client;

import com.challenge.client.rest.JsonPlaceholderClientRest;
import com.challenge.exception.*;
import com.challenge.model.Post;
import com.challenge.model.User;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;

import org.springframework.web.client.RestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class JsonPlaceholderClientRestTest {

    static WireMockServer wm;

    JsonPlaceholderClientRest client;

    @BeforeAll
    static void start() {
        wm = new WireMockServer(0);
        wm.start();
    }

    @AfterAll
    static void stop() { wm.stop(); }

    @BeforeEach
    void setUp() {
        var baseUrl = "http://localhost:" + wm.port();
        RestClient rc = RestClient.builder().baseUrl(baseUrl).build();
        client = new JsonPlaceholderClientRest(rc);
    }

    @Test
    void getPosts_ok() {
        wm.stubFor(get(urlEqualTo("/posts"))
                .willReturn(okJson("[{\"id\":1,\"userId\":10,\"title\":\"t\",\"body\":\"b\"}]")));

        List<Post> posts = client.getPosts();
        assertEquals(1, posts.size());
        assertEquals(1, posts.get(0).id());
    }

    @Test
    void getUser_404_mapsResourceNotFound() {
        wm.stubFor(get(urlEqualTo("/users/99")).willReturn(aResponse().withStatus(404)));
        assertThrows(ResourceNotFoundException.class, () -> client.getUser(99));
    }

    @Test
    void getPosts_400_mapsBadRequestValidation() {
        wm.stubFor(get(urlEqualTo("/posts")).willReturn(aResponse().withStatus(400)));
        assertThrows(BadRequestValidationException.class, () -> client.getPosts());
    }

    @Test
    void getPosts_429_mapsRateLimit() {
        wm.stubFor(get(urlEqualTo("/posts")).willReturn(aResponse().withStatus(429)));
        assertThrows(UpstreamRateLimitException.class, () -> client.getPosts());
    }

    @Test
    void getPosts_408_or_504_mapsTimeout() {
        wm.stubFor(get(urlEqualTo("/posts")).willReturn(aResponse().withStatus(504)));
        assertThrows(UpstreamTimeoutException.class, () -> client.getPosts());
    }

    @Test
    void deletePost_503_mapsUnavailable() {
        wm.stubFor(delete(urlEqualTo("/posts/1")).willReturn(aResponse().withStatus(503)));
        assertThrows(UpstreamUnavailableException.class, () -> client.deletePost(1));
    }

    @Test
    void getPosts_500_or_502_mapsServerError() {
        wm.stubFor(get(urlEqualTo("/posts")).willReturn(aResponse().withStatus(500)));
        assertThrows(UpstreamServerErrorException.class, () -> client.getPosts());
    }
}
