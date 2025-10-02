package com.challenge.exception;

import com.challenge.exception.error.ErrorCode;

public class UpstreamUnauthorizedException extends JsonPlaceholderException {
    public UpstreamUnauthorizedException() { super(ErrorCode.JPH_UNAUTHORIZED, ErrorCode.JPH_UNAUTHORIZED.message()); }
}
