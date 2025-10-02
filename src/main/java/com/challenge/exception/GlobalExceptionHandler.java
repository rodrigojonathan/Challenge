package com.challenge.exception;

import com.challenge.entrypoint.dto.DataResponse;
import com.challenge.exception.error.ApiError;
import com.challenge.exception.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String APP_PKG = "com.challenge";

    @ExceptionHandler(BaseAppException.class)
    public ResponseEntity<DataResponse> handleBase(BaseAppException ex) {
        Throwable root = rootCause(ex);
        String origin = originWithinApp(root);

        switch (ex.logLevel()) {
            case INFO -> log.info("{} | [{}] {} | at {}", Instant.now(), ex.code().code(), root.getMessage(), origin);
            case WARN -> log.warn("{} | [{}] {} | at {}", Instant.now(), ex.code().code(), root.getMessage(), origin);
            case ERROR -> log.error("{} | [{}] {} | at {}", Instant.now(), ex.code().code(), root.getMessage(), origin, ex);
        }

        int status = switch (ex.code()) {
            case JPH_BAD_REQUEST, BAD_REQUEST, VALIDATION -> 400;
            case JPH_UNAUTHORIZED -> 401;
            case JPH_FORBIDDEN -> 403;
            case JPH_POST_NOT_FOUND, JPH_USER_NOT_FOUND -> 404;
            case JPH_RATE_LIMIT -> 429;
            case JPH_UPSTREAM_UNAVAILABLE -> 503;
            case JPH_UPSTREAM_TIMEOUT -> 504;
            case JPH_UPSTREAM_ERROR -> 502;
            case APP_UNEXPECTED -> 500;
            default -> 500;
        };

        ApiError body = new ApiError(ex.code().code(), ex.code().message());
        return ResponseEntity.status(status).body(DataResponse.error(status, body));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<DataResponse> handleValidation(Exception ex) {
        Throwable root = rootCause(ex);
        String origin = originWithinApp(root);
        log.info("{} | [{}] {} | at {}", Instant.now(), ErrorCode.BAD_REQUEST.code(), root.getMessage(), origin);
        ApiError body = new ApiError(ErrorCode.BAD_REQUEST.code(), ErrorCode.BAD_REQUEST.message());
        return ResponseEntity.status(400).body(DataResponse.error(400, body));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DataResponse> handleFallback(Exception ex) {
        Throwable root = rootCause(ex);
        String origin = originWithinApp(root);
        log.error("{} | [{}] {} ({}) | at {}", Instant.now(),
                ErrorCode.APP_UNEXPECTED.code(), root.getMessage(), root.getClass().getSimpleName(), origin, ex);
        ApiError body = new ApiError(ErrorCode.APP_UNEXPECTED.code(), ErrorCode.APP_UNEXPECTED.message());
        return ResponseEntity.status(500).body(DataResponse.error(500, body));
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }

    private static String originWithinApp(Throwable t) {
        for (StackTraceElement ste : t.getStackTrace()) {
            if (ste.getClassName().startsWith(APP_PKG)) {
                return ste.getClassName() + "#" + ste.getMethodName() + ":" + ste.getLineNumber();
            }
        }
        StackTraceElement ste = t.getStackTrace().length > 0 ? t.getStackTrace()[0] : null;
        return ste == null ? "unknown" : ste.getClassName() + "#" + ste.getMethodName() + ":" + ste.getLineNumber();
    }
}
