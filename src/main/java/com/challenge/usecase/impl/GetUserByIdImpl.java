package com.challenge.usecase.impl;

import com.challenge.client.JsonPlaceholderClient;
import com.challenge.model.User;
import com.challenge.usecase.GetUserById;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetUserByIdImpl implements GetUserById {

    private final JsonPlaceholderClient client;

    @Autowired
    public GetUserByIdImpl(JsonPlaceholderClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "jph")
    @Retry(name = "jph")
    @Bulkhead(name = "jph", type = Bulkhead.Type.SEMAPHORE)
    @Override
    public User execute(Integer userId) {
        return client.getUser(userId);
    }
}
