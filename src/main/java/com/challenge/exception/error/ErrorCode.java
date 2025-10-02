package com.challenge.exception.error;

public enum ErrorCode {

    JPH_BAD_REQUEST        ("JPH01", "Invalid request to upstream"),
    JPH_UNAUTHORIZED       ("JPH02", "Unauthorized with upstream"),
    JPH_FORBIDDEN          ("JPH03", "Forbidden by upstream"),
    JPH_POST_NOT_FOUND     ("JPH04", "Post not found"),
    JPH_USER_NOT_FOUND     ("JPH05", "User not found"),
    JPH_RATE_LIMIT         ("JPH06", "Upstream rate limit exceeded"),

    JPH_UPSTREAM_TIMEOUT   ("JPH07", "Upstream timeout"),
    JPH_UPSTREAM_UNAVAILABLE("JPH08", "Upstream unavailable"),
    JPH_UPSTREAM_ERROR     ("JPH09", "Upstream server error"),

    VALIDATION             ("APP01", "Validation failed"),
    BAD_REQUEST            ("APP02", "Invalid input"),
    APP_UNEXPECTED         ("APP00", "Unexpected error");

    private final String code;
    private final String message;
    ErrorCode(String code, String message) { this.code = code; this.message = message; }

    public String code() { return code; }
    public String message() { return message; }
}
