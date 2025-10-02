package com.challenge.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record PostResponseDTO(
        @NotNull @Positive Integer postId,
        @NotBlank String title,
        @NotBlank String body,
        @NotNull @Valid AuthorDTO author,
        @NotNull List<@Valid CommentDTO> comments,
        @NotNull @Positive Integer commentsCount
) {}
