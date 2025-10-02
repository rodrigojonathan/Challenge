package com.challenge.usecase;

import com.challenge.dto.PostResponseDTO;

import java.util.List;

@FunctionalInterface
public interface BuildPostFullInfo { List<PostResponseDTO> getPosts(); }

