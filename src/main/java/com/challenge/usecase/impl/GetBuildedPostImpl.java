package com.challenge.usecase.impl;

import com.challenge.dto.PostResponseDTO;
import com.challenge.usecase.GetBuildedPost;
import com.challenge.usecase.BuildPostFullInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetBuildedPostImpl implements GetBuildedPost {

    private final BuildPostFullInfo catalog;

    public GetBuildedPostImpl(BuildPostFullInfo catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<PostResponseDTO> execute() {
        return catalog.getPosts();
    }
}
