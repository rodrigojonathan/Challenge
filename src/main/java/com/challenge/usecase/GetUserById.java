package com.challenge.usecase;

import com.challenge.model.User;

@FunctionalInterface
public interface GetUserById { User execute(Integer userId); }

