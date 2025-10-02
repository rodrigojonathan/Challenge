package com.challenge.usecase;

import com.challenge.dto.PostResponseDTO;
import com.challenge.usecase.impl.PegeablePostsImpl;
import com.challenge.util.TextSanitizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.challenge.TestDataFactory.dtoComment;
import static com.challenge.TestDataFactory.dtoPost;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PegeablePostsImplTest {

    BuildPostFullInfo catalog = mock(BuildPostFullInfo.class);
    TextSanitizer sanitizer = mock(TextSanitizer.class);

    PegeablePostsImpl sut = new PegeablePostsImpl(catalog, sanitizer);

    @Test
    void page_sortsAndSlices_andSanitizes() {
        var c1 = List.of(
                dtoComment(2, " a@b.com ", " hola "),
                dtoComment(1, "b@b.com", "chau")
        );
        var c2 = List.of(
                dtoComment(3, "c@b.com", "x")
        );

        var all = List.of(
                dtoPost(2, "bbb", " body2 ", 2, "  Ana ",  " ana@x.com ", c2),
                dtoPost(1, "aaa", " body1 ", 1, "  Juan ", " juan@x.com ", c1)
        );
        when(catalog.getPosts()).thenReturn(all);

        when(sanitizer.text(" body2 ")).thenReturn("body2");
        when(sanitizer.text(" body1 ")).thenReturn("body1");
        when(sanitizer.text("  Ana ")).thenReturn("Ana");
        when(sanitizer.text("  Juan ")).thenReturn("Juan");
        when(sanitizer.emailTrim(" ana@x.com ")).thenReturn("ana@x.com");
        when(sanitizer.emailTrim(" juan@x.com ")).thenReturn("juan@x.com");

        when(sanitizer.emailTrim("c@b.com")).thenReturn("c@b.com");
        when(sanitizer.text("x")).thenReturn("x");

        when(sanitizer.emailTrim(" a@b.com ")).thenReturn("a@b.com");
        when(sanitizer.emailTrim("b@b.com")).thenReturn("b@b.com");
        when(sanitizer.text(" hola ")).thenReturn("hola");
        when(sanitizer.text("chau")).thenReturn("chau");

        var page = sut.page(0, 1, "title,desc");

        assertAll("page metadata",
                () -> assertEquals(0, page.page()),
                () -> assertEquals(1, page.size()),
                () -> assertEquals(2, page.totalPages()),
                () -> assertTrue(page.hasNext()),
                () -> assertFalse(page.hasPrevious())
        );

        PostResponseDTO first = page.content().getFirst();
        assertAll("sanitized fields",
                () -> assertEquals("body2", first.body()),
                () -> assertEquals("Ana", first.author().name()),
                () -> assertEquals("ana@x.com", first.author().email())
        );

        var c = first.comments().getFirst();
        assertEquals("x", c.body());
        assertEquals("c@b.com", c.email());
    }
}
