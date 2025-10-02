package com.challenge.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CommentDTO(
        @NotNull @Positive Integer id,
        @NotBlank @Email String email,
        @NotBlank String body
) {}
