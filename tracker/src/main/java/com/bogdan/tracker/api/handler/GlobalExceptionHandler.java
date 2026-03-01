package com.bogdan.tracker.api.handler;

import com.bogdan.tracker.domain.exception.ErrorResponse;
import com.bogdan.tracker.domain.exception.Peer.PeerAlreadyExistsException;
import com.bogdan.tracker.domain.exception.Peer.PeerNotFoundException;
import com.bogdan.tracker.domain.exception.fileInfo.FileInfoAlreadyExistException;
import com.bogdan.tracker.domain.exception.fileInfo.FileInfoNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PeerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePeerAlreadyExists(PeerAlreadyExistsException ex) {
        log.error("Peer already exists: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, "Peer already exists", ex.getMessage());
    }

    @ExceptionHandler(PeerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePeerNotFound(PeerNotFoundException ex) {
        log.error("Peer not found: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "Peer not found", ex.getMessage());
    }

    @ExceptionHandler(FileInfoAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleFileInfoAlreadyExists(FileInfoAlreadyExistException ex) {
        log.error("File info already exists: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, "File already exists", ex.getMessage());
    }

    @ExceptionHandler(FileInfoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileInfoNotFound(FileInfoNotFoundException ex) {
        log.error("File info not found: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "File not found", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred"
        );
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String error, String message) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .build();
        return new ResponseEntity<>(errorResponse, status);
    }
}