package com.challenge.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthorDTO(
        @NotNull Integer id,
        @NotBlank String name,
        @Email String email
) {}

