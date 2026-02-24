package com.bogdan.tracker.domain.exception.fileInfo;

public class FileInfoAlreadyExistException extends RuntimeException {
    public FileInfoAlreadyExistException(String message) {
        super(message);
    }
}
