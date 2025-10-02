package com.challenge.usecase.impl;

import com.challenge.client.JsonPlaceholderClient;
import com.challenge.model.Post;
import com.challenge.usecase.GetAllPosts;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAllPostsImpl implements GetAllPosts {

    private final JsonPlaceholderClient client;

    @Autowired
    public GetAllPostsImpl(JsonPlaceholderClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "jph")
    @Retry(name = "jph")
    @Bulkhead(name = "jph", type = Bulkhead.Type.SEMAPHORE)
    @Override
    public List<Post> execute() {
        return client.getPosts();
    }
}
