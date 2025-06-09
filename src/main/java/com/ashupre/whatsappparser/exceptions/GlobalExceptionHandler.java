package com.ashupre.whatsappparser.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;


@RestControllerAdvice // needed for using the methods in it as exceptionHandlers
public class GlobalExceptionHandler {

    /**
     * whenever this type of exception happens, the method will be called and the response will be returned to the
     * api caller
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(ChatDeletionCountMismatchException.class)
    public ResponseEntity<String> handleChatDeletionCountMismatchException(ChatDeletionCountMismatchException ex) {
        return ResponseEntity.status(500).body("Error while deleting chats : " + ex.getMessage());
    }

    /**
     * This works because for now IOException is thrown only at /api/files/dummy. Modify if other places throw
     * IOException.
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException ex) {
        System.out.println("exception caught : " + ex.getMessage());
        return ResponseEntity.status(500).body("Internal server error : " + ex.getMessage());
    }
}
