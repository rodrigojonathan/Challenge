package com.challenge.exception;

import com.challenge.exception.error.ErrorCode;

public abstract class BusinessException extends BaseAppException {
    protected BusinessException(ErrorCode code, String message) {
        super(code, message, LogLevel.WARN);
    }

    protected BusinessException(ErrorCode code, String message, LogLevel level) {
        super(code, message, level);
    }
}
