package com.challenge.exception;

import com.challenge.exception.error.ErrorCode;

public class UpstreamRateLimitException extends JsonPlaceholderException {
    public UpstreamRateLimitException() { super(ErrorCode.JPH_RATE_LIMIT, ErrorCode.JPH_RATE_LIMIT.message(), LogLevel.WARN); }
}
