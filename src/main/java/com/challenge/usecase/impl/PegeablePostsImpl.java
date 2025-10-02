package com.challenge.usecase.impl;

import com.challenge.dto.AuthorDTO;
import com.challenge.dto.CommentDTO;
import com.challenge.dto.PostResponseDTO;
import com.challenge.dto.response.PageResponse;
import com.challenge.usecase.PegeablePosts;
import com.challenge.usecase.BuildPostFullInfo;
import com.challenge.util.TextSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PegeablePostsImpl implements PegeablePosts {

    private final BuildPostFullInfo catalog;
    private final TextSanitizer sanitizer;

    @Autowired
    public PegeablePostsImpl(BuildPostFullInfo catalog, TextSanitizer sanitizer) {
        this.catalog = catalog;
        this.sanitizer = sanitizer;
    }

    @Override
    public PageResponse<PostResponseDTO> page(Integer page, Integer size, String sort) {
        List<PostResponseDTO> all = catalog.getPosts();

        Integer p = Optional.ofNullable(page).filter(x -> x >= 0).orElse(0);
        Integer s = Optional.ofNullable(size).filter(x -> x > 0 && x <= 100).orElse(20);

        List<PostResponseDTO> sorted = applySort(all, sort);

        int total = sorted.size();
        int from = Math.min(p * s, total);
        int to = Math.min(from + s, total);

        List<PostResponseDTO> slice = sorted.subList(from, to)
                .stream()
                .map(this::sanitizeOne)
                .toList();

        int totalPages = (int) Math.ceil((double) total / s.doubleValue());
        Boolean hasNext = p + 1 < totalPages;
        Boolean hasPrevious = p > 0;

        return new PageResponse<>(slice, p, s, (long) total, totalPages, hasNext, hasPrevious);
    }

    private List<PostResponseDTO> applySort(List<PostResponseDTO> data, String sort) {
        if (sort == null || sort.isBlank()) {
            return data;
        }
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        boolean asc = parts.length < 2 || !"desc".equalsIgnoreCase(parts[1].trim());

        Map<String, Comparator<PostResponseDTO>> by = Map.of(
                "postId", Comparator.comparing(PostResponseDTO::postId),
                "title", Comparator.comparing(PostResponseDTO::title, Comparator.nullsLast(String::compareToIgnoreCase)),
                "commentsCount", Comparator.comparing(PostResponseDTO::commentsCount)
        );

        Comparator<PostResponseDTO> cmp = Optional.ofNullable(by.get(field)).orElse(by.get("postId"));
        return data.stream().sorted(asc ? cmp : cmp.reversed()).toList();
    }

    private PostResponseDTO sanitizeOne(PostResponseDTO p) {
        AuthorDTO a = p.author();
        AuthorDTO author = new AuthorDTO(a.id(), sanitizer.text(a.name()), sanitizer.emailTrim(a.email()));

        List<CommentDTO> comments = p.comments().stream()
                .map(c -> new CommentDTO(c.id(), sanitizer.emailTrim(c.email()), sanitizer.text(c.body())))
                .toList();

        return new PostResponseDTO(
                p.postId(),
                sanitizer.text(p.title()),
                sanitizer.text(p.body()),
                author,
                comments,
                p.commentsCount()
        );
    }
}
