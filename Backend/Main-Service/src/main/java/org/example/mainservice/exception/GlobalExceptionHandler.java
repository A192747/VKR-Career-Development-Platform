package org.example.mainservice.exception;

import org.apache.logging.log4j.util.InternalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({IOException.class, InternalException.class, InvalidKeyException.class, NoSuchAlgorithmException.class, IllegalArgumentException.class})
    public ExceptionResponse handleServerExceptions(Exception exception) {
        return createResponse(exception);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoSuchElementException.class, ResourceNotFoundException.class})
    public ExceptionResponse handleNotFoundExceptions(Exception exception) {
        return createResponse(exception);
    }

    private ExceptionResponse createResponse(Exception exception) {
        return new ExceptionResponse(
                List.of(exception.getMessage()),
                new Date(System.currentTimeMillis())
        );
    }
}
