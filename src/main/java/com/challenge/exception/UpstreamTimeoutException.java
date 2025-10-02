package com.challenge.exception;

import com.challenge.exception.error.ErrorCode;

public class UpstreamTimeoutException extends JsonPlaceholderException {
    public UpstreamTimeoutException() { super(ErrorCode.JPH_UPSTREAM_TIMEOUT, ErrorCode.JPH_UPSTREAM_TIMEOUT.message(), LogLevel.ERROR); }
}
