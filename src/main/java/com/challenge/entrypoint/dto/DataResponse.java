package com.challenge.entrypoint.dto;

import com.challenge.exception.error.ApiError;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DataResponse(Integer status, Object data, ApiError error) {
    public static DataResponse ok(Object data){ return new DataResponse(200, data, null); }
    public static DataResponse error(int status, ApiError err){ return new DataResponse(status, null, err); }
}
