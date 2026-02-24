package com.bogdan.tracker.domain.exception.Peer;

public class PeerNotFoundException extends RuntimeException {
    public PeerNotFoundException(String message) {
        super(message);
    }
}
