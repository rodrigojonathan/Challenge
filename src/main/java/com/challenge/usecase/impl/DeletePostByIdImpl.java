package com.challenge.usecase.impl;

import com.challenge.client.JsonPlaceholderClient;
import com.challenge.exception.error.ErrorCode;
import com.challenge.exception.ResourceNotFoundException;
import com.challenge.usecase.DeletePostById;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import static com.challenge.config.RedisCacheConfig.POSTS_ALL_CACHE;

@Component
public class DeletePostByIdImpl implements DeletePostById {

    private final JsonPlaceholderClient client;

    public DeletePostByIdImpl(JsonPlaceholderClient client) {
        this.client = client;
    }

    @CacheEvict(value = POSTS_ALL_CACHE, allEntries = true)
    @CircuitBreaker(name = "jph")
    @Retry(name = "jph")
    @Bulkhead(name = "jph", type = Bulkhead.Type.SEMAPHORE)
    @Override
    public void execute(Integer postId) {
        client.deletePost(postId);
    }
}
