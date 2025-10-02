package com.challenge.exception;

import com.challenge.entrypoint.dto.DataResponse;
import com.challenge.exception.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBase_mapsStatusAndLogsAtProperLevel() {
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        BaseAppException ex = Mockito.mock(BaseAppException.class, Mockito.CALLS_REAL_METHODS);
        Mockito.when(ex.code()).thenReturn(ErrorCode.JPH_POST_NOT_FOUND);
        Mockito.when(ex.logLevel()).thenReturn(BaseAppException.LogLevel.ERROR);
        Mockito.when(ex.getMessage()).thenReturn("not found");
        Mockito.when(ex.getCause()).thenReturn(new RuntimeException("root"));

        ResponseEntity<DataResponse> resp = handler.handleBase(ex);

        assertAll(
                () -> assertEquals(404, resp.getStatusCode().value()),
                () -> {
                    assertNotNull(resp.getBody());
                    assertEquals(ErrorCode.JPH_POST_NOT_FOUND.code(),
                            resp.getBody().error().code());
                }
        );

        assertFalse(appender.list.isEmpty());
        var last = appender.list.getLast();
        assertTrue(last.getFormattedMessage().contains(ErrorCode.JPH_POST_NOT_FOUND.code()));
        assertEquals("ERROR", last.getLevel().toString());
    }

    @Test
    void handleValidation_returns400() {
        ResponseEntity<DataResponse> resp = handler.handleValidation(new IllegalArgumentException("bad"));
        assertEquals(400, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertEquals(ErrorCode.BAD_REQUEST.code(), resp.getBody().error().code());
    }

    @Test
    void handleFallback_returns500_andLogsError() {
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        ResponseEntity<DataResponse> resp = handler.handleFallback(new RuntimeException("boom"));
        assertEquals(500, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertEquals(ErrorCode.APP_UNEXPECTED.code(), resp.getBody().error().code());

        var last = appender.list.getLast();
        assertEquals("ERROR", last.getLevel().toString());
        assertTrue(last.getFormattedMessage().contains(ErrorCode.APP_UNEXPECTED.code()));
    }
}
