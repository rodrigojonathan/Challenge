package com.challenge.exception;

import com.challenge.exception.error.ErrorCode;

public abstract class ValidationException extends BaseAppException {
    protected ValidationException(ErrorCode code, String message) {
        super(code, message, LogLevel.INFO);
    }
}
