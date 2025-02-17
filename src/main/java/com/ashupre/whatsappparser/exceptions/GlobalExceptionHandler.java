package com.ashupre.whatsappparser.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice // needed for using the methods in it as exceptionhandlers
public class GlobalExceptionHandler {

    /**
     * whenever this type of exception happens, the method will be called and the response will be returned to the
     * api caller
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
