package com.example.authentication.exception;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorResponse> handleException(EntityExistsException e) {
        return generateDefaultErrorResponse(BAD_REQUEST, e);
    }

    @ExceptionHandler(ActivationCodeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(ActivationCodeNotFoundException e) {
        return generateDefaultErrorResponse(NOT_FOUND, e);
    }

    @ExceptionHandler(ActivationCodeExpiredException.class)
    public ResponseEntity<ErrorResponse> handleException(ActivationCodeExpiredException e) {
        return generateDefaultErrorResponse(GONE, e);
    }

    @ExceptionHandler(AccountNotActivatedException.class)
    public ResponseEntity<ErrorResponse> handleException(AccountNotActivatedException e) {
        return generateDefaultErrorResponse(FORBIDDEN, e);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(EntityNotFoundException e) {
        return generateDefaultErrorResponse(NOT_FOUND, e);
    }

    private ResponseEntity<ErrorResponse> generateDefaultErrorResponse(HttpStatus status, Exception e) {
        ErrorResponse error = new ErrorResponse();

        error.setCode(status.value());
        error.setMessage(e.getMessage());
        error.setTimestamp(System.currentTimeMillis());

        return new ResponseEntity<>(error, status);
    }
}
