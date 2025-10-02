package com.challenge.usecase.impl;

import com.challenge.client.JsonPlaceholderClient;
import com.challenge.model.Comment;
import com.challenge.usecase.GetCommentsByPost;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetCommentsByPostImpl implements GetCommentsByPost {

    private final JsonPlaceholderClient client;

    @Autowired
    public GetCommentsByPostImpl(JsonPlaceholderClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "jph")
    @Retry(name = "jph")
    @Bulkhead(name = "jph", type = Bulkhead.Type.SEMAPHORE)
    @Override
    public List<Comment> execute(Integer postId) {
        return client.getCommentsByPost(postId);
    }
}
