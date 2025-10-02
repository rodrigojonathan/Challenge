package com.challenge.usecase;

import com.challenge.client.JsonPlaceholderClient;
import com.challenge.dto.PostResponseDTO;
import com.challenge.model.Comment;
import com.challenge.model.Post;
import com.challenge.model.User;
import com.challenge.dto.response.PageResponse;
import com.challenge.util.TestExecutorConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.challenge.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
        properties = {
                "spring.cache.type=NONE",
                "springdoc.api-docs.enabled=false",
                "springdoc.swagger-ui.enabled=false",
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration",
                "spring.main.web-application-type=none"
        }
)
@ActiveProfiles("test")
@Import({ TestExecutorConfig.class, UseCasesCompilationSmokeTest.ClientMockConfig.class, UseCasesCompilationSmokeTest.TestRedisConfig.class })
class UseCasesCompilationSmokeTest {

    @TestConfiguration
    static class ClientMockConfig {
        @Bean @Primary
        JsonPlaceholderClient jsonPlaceholderClient() {
            return org.mockito.Mockito.mock(JsonPlaceholderClient.class);
        }
    }

    @TestConfiguration
    static class TestRedisConfig {
        @Bean
        LettuceConnectionFactory lettuceConnectionFactory() {
            return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
        }
    }

    @Autowired ApplicationContext ctx;
    @Autowired GetAllPosts getAllPosts;
    @Autowired GetUserById getUserById;
    @Autowired GetCommentsByPost getCommentsByPost;
    @Autowired DeletePostById deletePostById;
    @Autowired BuildPostFullInfo buildPostFullInfo;
    @Autowired PegeablePosts pegeablePosts;
    @Autowired JsonPlaceholderClient client;

    @Test
    void contextLoads_andAllUseCasesAreWired() {
        assertAll(
                () -> assertNotNull(ctx),
                () -> assertNotNull(getAllPosts),
                () -> assertNotNull(getUserById),
                () -> assertNotNull(getCommentsByPost),
                () -> assertNotNull(deletePostById),
                () -> assertNotNull(buildPostFullInfo),
                () -> assertNotNull(pegeablePosts)
        );
    }
}
