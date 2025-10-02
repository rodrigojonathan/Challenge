package com.challenge;

import com.challenge.dto.AuthorDTO;
import com.challenge.dto.CommentDTO;
import com.challenge.dto.PostResponseDTO;
import com.challenge.model.Comment;
import com.challenge.model.Post;
import com.challenge.model.User;

import java.util.List;

public final class TestDataFactory {
    private TestDataFactory() {}

    public static User user(int id, String name, String email) {
        return new User(id, name, email);
    }

    public static Post post(int id, int userId, String title, String body) {
        return new Post(id, userId, title, body);
    }

    public static Comment comment(int id, int postId, String name, String email, String body) {
        return new Comment(id, postId, name, email, body);
    }

    public static CommentDTO dtoComment(int id, String email, String body) {
        return new CommentDTO(id, email, body);
    }

    public static PostResponseDTO dtoPost(int postId, String title, String body,
                                          int authorId, String authorName, String authorEmail,
                                          List<CommentDTO> comments) {
        return new PostResponseDTO(
                postId,
                title,
                body,
                new AuthorDTO(authorId, authorName, authorEmail),
                comments,
                comments.size()
        );
    }
}