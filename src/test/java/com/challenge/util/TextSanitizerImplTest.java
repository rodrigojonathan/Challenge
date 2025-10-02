package com.challenge.util;

import com.challenge.util.impl.TextSanitizerImpl;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextSanitizerImplTest {

    TextSanitizerImpl sanitizer = new TextSanitizerImpl(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void text_trimAndCollapseSpaces_returnsNullIfEmpty() {
        assertAll(
                () -> assertNull(sanitizer.text(null)),
                () -> assertNull(sanitizer.text("   ")),
                () -> assertEquals("hola mundo", sanitizer.text("  hola   mundo   "))
        );
    }

    @Test
    void emailTrim_validatesAndTrims() {
        assertAll(
                () -> assertNull(sanitizer.emailTrim(null)),
                () -> assertEquals("a@b.com", sanitizer.emailTrim("  a@b.com  "))
        );
    }

    @Test
    void emailTrim_throwsOnInvalid() {
        var ex = assertThrows(IllegalArgumentException.class, () -> sanitizer.emailTrim("  no-es-mail  "));
        assertTrue(ex.getMessage().contains("email inválido"));
    }
}
