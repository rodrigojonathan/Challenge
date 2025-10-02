package com.challenge.usecase;

import com.challenge.dto.PostResponseDTO;
import com.challenge.dto.response.PageResponse;

public interface PegeablePosts {
    PageResponse<PostResponseDTO> page(Integer page, Integer size, String sort);
}

