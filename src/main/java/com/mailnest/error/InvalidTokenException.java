package com.mailnest.error;

public class InvalidTokenException extends AppException {
  public InvalidTokenException(String token) {
    super("Invalid or expired token: " + token);
  }
}
