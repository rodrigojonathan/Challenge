package com.challenge.client;

import com.challenge.model.Comment;
import com.challenge.model.Post;
import com.challenge.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface JsonPlaceholderClient {

    List<Post> getPosts();
    List<Comment> getCommentsByPost(Integer postId);
    User getUser(Integer userId);
    void deletePost(Integer postId);
}

