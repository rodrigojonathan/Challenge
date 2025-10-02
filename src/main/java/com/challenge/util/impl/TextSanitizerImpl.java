package com.challenge.util.impl;

import com.challenge.util.TextSanitizer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
public class TextSanitizerImpl implements TextSanitizer {

    private final Validator validator;

    public TextSanitizerImpl(Validator validator) {
        this.validator = validator;
    }

    @Override
    public String text(String input) {
        String t = Objects.toString(input, null);
        if (t == null) return null;
        String trimmed = t.trim().replaceAll("\\s+", " ");
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public String emailTrim(String input) {
        String e = Objects.toString(input, null);
        if (e == null) return null;
        String candidate = e.trim();
        emailValidator(candidate);
        return candidate;
    }

    private void emailValidator(String value) {
        EmailHolder holder = new EmailHolder(value);
        Set<ConstraintViolation<EmailHolder>> violations = validator.validate(holder);
        violations.stream().findFirst().map(ConstraintViolation::getMessage)
                .ifPresent(m -> { throw new IllegalArgumentException("email inválido: " + value); });
    }

    public static final class EmailHolder {
        @Email
        private final String value;
        public EmailHolder(String value) { this.value = value; }
        public String getValue() { return value; }
    }
}
