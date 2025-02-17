package org.example.mainservice.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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
import java.util.stream.Collectors;

@Slf4j
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
        log.warn(exception.getMessage(), exception);
        return new ExceptionResponse(
                List.of(exception.getMessage()),
                new Date(System.currentTimeMillis())
        );
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ExceptionResponse handleConstraintViolationException(ConstraintViolationException exception) {
        List<String> violations = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        return new ExceptionResponse(violations, new Date());
    }


    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ExceptionResponse handleAccessDeniedException(AccessDeniedException exception) {
        return createResponse(exception);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadRequestException.class, UnexpectedTypeException.class})
    public ExceptionResponse handleBadRequestException(Exception exception) {
        return createResponse(exception);
    }
}
