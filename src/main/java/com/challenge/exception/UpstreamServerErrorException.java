package com.challenge.exception;

import com.challenge.exception.error.ErrorCode;

public class UpstreamServerErrorException extends JsonPlaceholderException {
    public UpstreamServerErrorException() { super(ErrorCode.JPH_UPSTREAM_ERROR, ErrorCode.JPH_UPSTREAM_ERROR.message(), LogLevel.ERROR); }
}
