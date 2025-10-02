package com.challenge.exception;

import com.challenge.exception.error.ErrorCode;

public class UpstreamForbiddenException extends JsonPlaceholderException {
    public UpstreamForbiddenException() { super(ErrorCode.JPH_FORBIDDEN, ErrorCode.JPH_FORBIDDEN.message()); }
}
