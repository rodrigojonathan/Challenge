package com.challenge.usecase;

import com.challenge.model.Post;
import java.util.List;

@FunctionalInterface
public interface GetAllPosts { List<Post> execute(); }

