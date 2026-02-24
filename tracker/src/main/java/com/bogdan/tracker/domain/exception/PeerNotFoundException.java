package com.bogdan.tracker.domain.exception;

public class PeerNotFoundException extends RuntimeException {
  public PeerNotFoundException(String message) {
    super(message);
  }
}
