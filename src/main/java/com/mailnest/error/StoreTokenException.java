package com.mailnest.error;

public class StoreTokenException extends AppException {
  public StoreTokenException(Throwable cause) {
    super("Database error while storing subscription token", cause);
  }
}
