package com.challenge.usecase.impl;

import com.challenge.dto.AuthorDTO;
import com.challenge.dto.CommentDTO;
import com.challenge.dto.PostResponseDTO;
import com.challenge.model.Comment;
import com.challenge.model.Post;
import com.challenge.model.User;
import com.challenge.usecase.GetAllPosts;
import com.challenge.usecase.GetCommentsByPost;
import com.challenge.usecase.GetUserById;
import com.challenge.usecase.BuildPostFullInfo;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static com.challenge.config.RedisCacheConfig.POSTS_ALL_CACHE;

@Service
public class BuildPostFullInfoImpl implements BuildPostFullInfo {

    private final GetAllPosts getAllPosts;
    private final GetUserById getUserById;
    private final GetCommentsByPost getCommentsByPost;
    private final ExecutorService executor;

    public BuildPostFullInfoImpl(GetAllPosts getAllPosts,
                                 GetUserById getUserById,
                                 GetCommentsByPost getCommentsByPost,
                                 ExecutorService virtualThread) {
        this.getAllPosts = getAllPosts;
        this.getUserById = getUserById;
        this.getCommentsByPost = getCommentsByPost;
        this.executor = virtualThread;
    }

    @Override
    @Cacheable(cacheNames = POSTS_ALL_CACHE)
    public List<PostResponseDTO> getPosts() {
        List<Post> posts = getAllPosts.execute();
        Map<Integer, CompletableFuture<User>> usersCache = new ConcurrentHashMap<>();

        return posts.stream()
                .map(p -> {
                    CompletableFuture<User> uf = usersCache.computeIfAbsent(
                            p.userId(),
                            id -> CompletableFuture
                                    .supplyAsync(() -> getUserById.execute(id), executor)
                                    .exceptionally(ex -> null)
                    );

                    CompletableFuture<List<Comment>> cf = CompletableFuture
                            .supplyAsync(() -> getCommentsByPost.execute(p.id()), executor)
                            .exceptionally(ex -> List.of());

                    User u = uf.join();
                    List<CommentDTO> comments = cf.join()
                            .stream()
                            .map(c -> new CommentDTO(c.id(), c.email(), c.body()))
                            .sorted(Comparator.comparing(CommentDTO::id))
                            .toList();

                    AuthorDTO author = (u != null)
                            ? new AuthorDTO(u.id(), u.name(), u.email())
                            : new AuthorDTO(0, "unknown", null);

                    return new PostResponseDTO(
                            p.id(), p.title(), p.body(),
                            author, comments, comments.size()
                    );
                })
                .sorted(Comparator.comparing(PostResponseDTO::postId))
                .toList();
    }

}
