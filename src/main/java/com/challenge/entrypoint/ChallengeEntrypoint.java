package com.challenge.entrypoint;

import com.challenge.dto.PostResponseDTO;
import com.challenge.dto.response.PageResponse;
import com.challenge.entrypoint.dto.DataResponse;
import com.challenge.usecase.DeletePostById;
import com.challenge.usecase.GetBuildedPost;
import com.challenge.usecase.PegeablePosts;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@Validated
public class ChallengeEntrypoint {

    private final GetBuildedPost GetBuildedPost;
    private final DeletePostById deletePostById;
    private final PegeablePosts pegeablePosts;

    public ChallengeEntrypoint(GetBuildedPost GetBuildedPost, DeletePostById deletePostById, PegeablePosts pegeablePosts) {
        this.GetBuildedPost = GetBuildedPost;
        this.deletePostById = deletePostById;
        this.pegeablePosts = pegeablePosts;
    }

    @Operation(summary = "Obtiene posts con autor y comentarios (mergeados y procesados)")
    @GetMapping
    public ResponseEntity<DataResponse> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        if (page == null && size == null) {
            List<PostResponseDTO> data = GetBuildedPost.execute();
            return ResponseEntity.ok(DataResponse.ok(data));
        }
        PageResponse<PostResponseDTO> pageData = pegeablePosts.page(page, size, sort);
        return ResponseEntity.ok(DataResponse.ok(pageData));
    }

    @Operation(summary = "Elimina un post en JSONPlaceholder (simulado)")
    @DeleteMapping("/{id}")
    public ResponseEntity<DataResponse> delete(@PathVariable @Positive Integer id) {
        deletePostById.execute(id);
        return ResponseEntity.noContent().build();
    }
}
