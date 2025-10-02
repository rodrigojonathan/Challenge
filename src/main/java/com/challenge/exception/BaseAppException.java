package com.challenge.exception;

import com.challenge.exception.error.ErrorCode;

public abstract class BaseAppException extends RuntimeException {

    private final ErrorCode code;
    private final LogLevel logLevel;

    public enum LogLevel { INFO, WARN, ERROR }

    protected BaseAppException(ErrorCode code, String message, Throwable cause, LogLevel level) {
        super(message, cause);
        this.code = code;
        this.logLevel = level == null ? LogLevel.ERROR : level;
    }

    protected BaseAppException(ErrorCode code, String message, LogLevel level) {
        this(code, message, null, level);
    }

    public ErrorCode code() { return code; }
    public LogLevel logLevel() { return logLevel; }
}
