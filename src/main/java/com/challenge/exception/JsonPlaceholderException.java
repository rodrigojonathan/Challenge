package com.challenge.exception;

import com.challenge.exception.error.ErrorCode;

public abstract class JsonPlaceholderException extends BusinessException {
    protected JsonPlaceholderException(ErrorCode code, String message) {
        super(code, message);
    }

    protected JsonPlaceholderException(ErrorCode code, String message, LogLevel level) {
        super(code, message, level);
    }
}

