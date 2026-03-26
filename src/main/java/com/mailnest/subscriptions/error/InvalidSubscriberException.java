package com.mailnest.subscriptions.error;

public class InvalidSubscriberException extends RuntimeException {
  public InvalidSubscriberException(String message) {
    super(message);
  }
}
