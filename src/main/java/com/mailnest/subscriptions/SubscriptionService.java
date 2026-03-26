package com.mailnest.subscriptions;

import com.mailnest.domain.NewSubscriber;
import com.mailnest.email.EmailClient;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

  private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);
  private final SubscriberRepository repository;
  private final Tracer tracer;
  private final EmailClient emailClient;

  public SubscriptionService(
      SubscriberRepository repository, Tracer tracer, EmailClient emailClient) {
    this.repository = repository;
    this.tracer = tracer;
    this.emailClient = emailClient;
  }

  public void subscribe(NewSubscriber newSubscriber) {
    Span span = tracer.nextSpan().name("add-new-subscriber");

    span.tag("operation", "add-new-subscriber");
    span.tag("subscriber.email_hash", hashEmail(newSubscriber.getEmail().asString()));

    try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
      log.info("Adding a new subscriber");

      insertSubscriber(newSubscriber);

      String confirmationLink =
          "http://localhost:8080/subscriptions/confirm?token=" + UUID.randomUUID();

      log.info("About to send confirmation email");

      emailClient.sendEmail(
          newSubscriber.getEmail(),
          "Confirm your subscription",
          "<html><body><a href=\""
              + confirmationLink
              + "\">Confirm your subscription</a></body></html>",
          "Visit " + confirmationLink);

      log.info("Confirmation email send call completed");

    } catch (Exception e) {
      span.error(e);
      log.error("Failed to add subscriber", e);
      throw e;
    } finally {
      span.end();
    }
  }

  private void insertSubscriber(NewSubscriber newSubscriber) {
    Span dbSpan = tracer.nextSpan().name("save-subscriber-to-database");

    dbSpan.tag("operation", "save-subscriber-to-database");
    dbSpan.tag("subscriber.email_hash", hashEmail(newSubscriber.getEmail().asString()));

    try (Tracer.SpanInScope ws = tracer.withSpan(dbSpan.start())) {
      log.info("Saving new subscriber details in the database");

      Subscriber subscriber = new Subscriber();
      subscriber.setId(UUID.randomUUID());
      subscriber.setEmail(newSubscriber.getEmail().asString());
      subscriber.setName(newSubscriber.getName().asString());
      subscriber.setSubscribedAt(OffsetDateTime.now());

      repository.save(subscriber);

      log.info("New subscriber details have been saved");
    } catch (Exception e) {
      dbSpan.error(e);
      log.error("Failed to execute query", e);
      throw e;
    } finally {
      dbSpan.end();
    }
  }

  private String hashEmail(String email) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
