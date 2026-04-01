package com.mailnest.error;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(InvalidSubscriberException.class)
  public ResponseEntity<?> handleInvalidSubscriber(InvalidSubscriberException e) {
    return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<?> handleInvalidToken(InvalidTokenException e) {
    return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
  }

  @ExceptionHandler(StoreTokenException.class)
  public ResponseEntity<Void> handleStoreToken(StoreTokenException e) {
    log.error("Database failure", e);
    return ResponseEntity.status(500).build();
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Void> handleGeneric(Exception e) {
    log.error("Unexpected error", e);
    return ResponseEntity.status(500).build();
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    BindException.class,
    MissingServletRequestParameterException.class
  })
  public ResponseEntity<?> handleValidation(Exception e) {
    return ResponseEntity.badRequest().body(Map.of("error", "Invalid subscription data"));
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<?> handleUnauthorized(UnauthorizedException e) {
    return ResponseEntity.status(401).header("WWW-Authenticate", "Basic realm=\"publish\"").build();
  }
}
