package com.challenge.client.rest;

import com.challenge.client.JsonPlaceholderClient;
import com.challenge.exception.*;
import com.challenge.exception.error.ErrorCode;
import com.challenge.model.Comment;
import com.challenge.model.Post;
import com.challenge.model.User;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class JsonPlaceholderClientRest implements JsonPlaceholderClient {

    public static final String POSTS = "/posts";
    public static final String POST_COMMENTS = "/posts/{id}/comments";
    public static final String USER_BY_ID = "/users/{id}";
    public static final String POST_BY_ID = "/posts/{id}";

    private final RestClient client;

    public JsonPlaceholderClientRest(RestClient client) {
        this.client = client;
    }

    @Override
    public List<Post> getPosts() {
        return exceptionGuard(client.get().uri(POSTS), null)
                .body(new ParameterizedTypeReference<>() {});
    }

    @Override
    public List<Comment> getCommentsByPost(Integer postId) {
        return exceptionGuard(client.get().uri(POST_COMMENTS, postId), ErrorCode.JPH_POST_NOT_FOUND)
                .body(new ParameterizedTypeReference<>() {});
    }

    @Override
    public User getUser(Integer userId) {
        return exceptionGuard(client.get().uri(USER_BY_ID, userId), ErrorCode.JPH_USER_NOT_FOUND)
                .body(User.class);
    }

    @Override
    public void deletePost(Integer postId) {
        exceptionGuard(client.delete().uri(POST_BY_ID, postId), ErrorCode.JPH_POST_NOT_FOUND)
                .toBodilessEntity();
    }

    private RestClient.ResponseSpec exceptionGuard(RestClient.RequestHeadersSpec<?> spec, ErrorCode notFoundCode) {
        return spec.retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    int sc = res.getStatusCode().value();
                    switch (sc) {
                        case 400 -> throw new BadRequestValidationException(ErrorCode.JPH_BAD_REQUEST.message());
                        case 401 -> throw new UpstreamUnauthorizedException();
                        case 403 -> throw new UpstreamForbiddenException();
                        case 404 -> { throw new ResourceNotFoundException(notFoundCode != null ? notFoundCode : ErrorCode.JPH_POST_NOT_FOUND); }
                        case 429 -> throw new UpstreamRateLimitException();
                        case 408, 504 -> throw new UpstreamTimeoutException();
                        case 503 -> throw new UpstreamUnavailableException();
                        case 500, 502 -> throw new UpstreamServerErrorException();
                        default -> throw new UpstreamServerErrorException();
                    }
                });
    }
}
