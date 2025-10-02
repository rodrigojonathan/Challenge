package com.challenge.exception;

import com.challenge.exception.error.ErrorCode;

public class BadRequestValidationException extends ValidationException {
    public BadRequestValidationException(String msg) {
        super(ErrorCode.BAD_REQUEST, msg != null ? msg : ErrorCode.BAD_REQUEST.message());
    }
}
