package com.challenge.exception;

import com.challenge.exception.error.ErrorCode;

public class UpstreamUnavailableException extends JsonPlaceholderException {
    public UpstreamUnavailableException() { super(ErrorCode.JPH_UPSTREAM_UNAVAILABLE, ErrorCode.JPH_UPSTREAM_UNAVAILABLE.message(), LogLevel.ERROR); }
}
