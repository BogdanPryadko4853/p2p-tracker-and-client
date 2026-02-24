package com.bogdan.tracker.domain.exception.Peer;

public class PeerAlreadyExistsException extends RuntimeException {
    public PeerAlreadyExistsException(String message) {
        super(message);
    }
}
