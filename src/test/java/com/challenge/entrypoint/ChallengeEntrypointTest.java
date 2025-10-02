package com.challenge.entrypoint;

import com.challenge.dto.response.PageResponse;
import com.challenge.usecase.DeletePostById;
import com.challenge.usecase.GetBuildedPost;
import com.challenge.usecase.PegeablePosts;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.challenge.TestDataFactory.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ChallengeEntrypoint.class)
@Import(ChallengeEntrypointTest.MvcMocksConfig.class)
class ChallengeEntrypointTest {

    @Autowired MockMvc mvc;
    @Autowired GetBuildedPost getBuildedPost;
    @Autowired PegeablePosts pegeablePosts;
    @Autowired DeletePostById deletePostById;

    @TestConfiguration
    static class MvcMocksConfig {
        @Bean GetBuildedPost getBuildedPost() { return Mockito.mock(GetBuildedPost.class); }
        @Bean PegeablePosts pegeablePosts() { return Mockito.mock(PegeablePosts.class); }
        @Bean DeletePostById deletePostById() { return Mockito.mock(DeletePostById.class); }
    }

    @Test
    void getAll_withoutPaging_returnsListEnvelope() throws Exception {
        var list = List.of(
                dtoPost(1, "t", "b", 1, "Ana", "a@x", List.of())
        );
        when(getBuildedPost.execute()).thenReturn(list);

        mvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].postId", is(1)));
    }

    @Test
    void getAll_withPaging_returnsPageEnvelope() throws Exception {
        var page = new PageResponse<>(
                List.of(dtoPost(1,"t","b", 1,"Ana","a@x", List.of())),
                0, 20, 1L, 1, false, false
        );
        when(pegeablePosts.page(0, 20, null)).thenReturn(page);

        mvc.perform(get("/posts?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.totalElements", is(1)));
    }

    @Test
    void delete_validatesPositiveId_andReturns204() throws Exception {
        mvc.perform(delete("/posts/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(deletePostById).execute(1);
    }
}
