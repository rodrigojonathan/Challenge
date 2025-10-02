package com.challenge.exception;

import com.challenge.exception.error.ErrorCode;

public class ResourceNotFoundException extends JsonPlaceholderException {
    public ResourceNotFoundException(ErrorCode code) { super(code, code.message()); }
}
