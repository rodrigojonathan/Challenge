package com.challenge.usecase;

import com.challenge.dto.PostResponseDTO;
import com.challenge.model.Post;
import com.challenge.usecase.impl.BuildPostFullInfoImpl;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.challenge.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuildPostFullInfoImplTest {

    private GetAllPosts getAllPosts;
    private GetUserById getUserById;
    private GetCommentsByPost getCommentsByPost;

    private static ExecutorService pool;

    @BeforeAll
    static void initPool() {
        pool = Executors.newFixedThreadPool(4);
    }

    @AfterAll
    static void shutdownPool() {
        pool.shutdownNow();
    }

    @BeforeEach
    void setUp() {
        getAllPosts = mock(GetAllPosts.class);
        getUserById = mock(GetUserById.class);
        getCommentsByPost = mock(GetCommentsByPost.class);
    }

    @Test
    void getPosts_mergesAndOrders_andCountsComments() {
        BuildPostFullInfo sut = new BuildPostFullInfoImpl(getAllPosts, getUserById, getCommentsByPost, pool);

        when(getAllPosts.execute()).thenReturn(List.of(
                post(1, 10, "t1", "b1"),
                post(2, 20, "t2", "b2")
        ));

        when(getUserById.execute(1)).thenReturn(user(1, "Ana", "ana@x.com"));
        when(getUserById.execute(2)).thenReturn(user(2, "Juan", "juan@x.com"));

        when(getCommentsByPost.execute(10)).thenReturn(List.of(
                comment(2, 1, "n2", "a@b.com", "x"),
                comment(1, 1, "n1", "c@b.com", "y")
        ));
        when(getCommentsByPost.execute(20)).thenReturn(List.of());

        List<PostResponseDTO> out = sut.getPosts();

        assertAll(
                () -> assertEquals(2, out.size()),
                () -> assertEquals(10, out.getFirst().postId()),
                () -> assertEquals(2, out.getFirst().commentsCount()),
                () -> assertTrue(out.getFirst().comments().stream().anyMatch(c -> c.id() == 2)),
                () -> assertEquals("Ana", out.getFirst().author().name())
        );

        verify(getAllPosts, times(1)).execute();
        verify(getUserById, times(1)).execute(1);
        verify(getUserById, times(1)).execute(2);
        verify(getCommentsByPost, times(1)).execute(10);
        verify(getCommentsByPost, times(1)).execute(20);
        verifyNoMoreInteractions(getAllPosts, getUserById, getCommentsByPost);
    }

    @Test
    void getPosts_runsParallel_fasterThanSequential() {
        BuildPostFullInfo sut = new BuildPostFullInfoImpl(getAllPosts, getUserById, getCommentsByPost, pool);

        when(getAllPosts.execute()).thenReturn(List.of(
                new Post(1, 10, "t1", "b1"),
                new Post(2, 20, "t2", "b2")
        ));

        doAnswer(a -> { Thread.sleep(200); return user(10,"a","a@x"); }).when(getUserById).execute(10);
        doAnswer(a -> { Thread.sleep(200); return user(20,"b","b@x"); }).when(getUserById).execute(20);
        doAnswer(a -> { Thread.sleep(200); return List.of(comment(1, 1, "n1", "e@x.com", "x")); })
                .when(getCommentsByPost).execute(1);
        doAnswer(a -> { Thread.sleep(200); return List.of(comment(2, 2, "n2", "e@x.com", "x")); })
                .when(getCommentsByPost).execute(2);

        long t0 = System.nanoTime();
        sut.getPosts();
        long ms = Duration.ofNanos(System.nanoTime() - t0).toMillis();

        assertTrue(ms < 600, "debería ejecutar concurrente, duró " + ms + "ms");
    }
}
