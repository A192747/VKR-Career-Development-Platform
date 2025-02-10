package org.example.mainservice.exception;

import java.util.Date;
import java.util.List;

public record ExceptionResponse(List<String> errors, Date timestamp) {
}
