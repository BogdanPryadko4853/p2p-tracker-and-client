package com.bogdan.tracker.domain.exception;

public class PeerAlreadyExistsException extends RuntimeException {
    public PeerAlreadyExistsException(String message) {
        super(message);
    }
}
