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
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

  private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);
  private final SubscriberRepository repository;
  private final SubscriptionTokenRepository tokenRepository;
  private final Tracer tracer;
  private final EmailClient emailClient;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification =
          "Spring-managed service dependency is injected via constructor and not exposed.")
  public SubscriptionService(
      SubscriberRepository repository,
      SubscriptionTokenRepository tokenRepository,
      Tracer tracer,
      EmailClient emailClient) {
    this.repository = repository;
    this.tokenRepository = tokenRepository;
    this.tracer = tracer;
    this.emailClient = emailClient;
  }

  public void subscribe(NewSubscriber newSubscriber) {
    Span span = tracer.nextSpan().name("add-new-subscriber");

    span.tag("operation", "add-new-subscriber");
    span.tag("subscriber.email_hash", hashEmail(newSubscriber.getEmail().asString()));

    try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
      log.info("Adding a new subscriber");

      Subscriber subscriber = insertSubscriber(newSubscriber);

      String token = UUID.randomUUID().toString();
      saveSubscriptionToken(subscriber.getId(), token);

      sendConfirmationEmail(newSubscriber, token);

      log.info("Confirmation email send call completed");

    } catch (Exception e) {
      span.error(e);
      log.error("Failed to add subscriber", e);
      throw e;
    } finally {
      span.end();
    }
  }

  private Subscriber insertSubscriber(NewSubscriber newSubscriber) {
    Span dbSpan = tracer.nextSpan().name("save-subscriber-to-database");

    dbSpan.tag("operation", "save-subscriber-to-database");
    dbSpan.tag("subscriber.email_hash", hashEmail(newSubscriber.getEmail().asString()));

    try (Tracer.SpanInScope ws = tracer.withSpan(dbSpan.start())) {
      log.info("Saving new subscriber details in the database");

      Subscriber subscriber = new Subscriber();
      subscriber.setId(UUID.randomUUID());
      subscriber.setEmail(newSubscriber.getEmail().asString());
      subscriber.setName(newSubscriber.getName().asString());
      subscriber.setStatus("pending_confirmation");
      subscriber.setSubscribedAt(OffsetDateTime.now());

      repository.save(subscriber);

      log.info("New subscriber details have been saved");

      return subscriber;
    } catch (Exception e) {
      dbSpan.error(e);
      log.error("Failed to execute query", e);
      throw e;
    } finally {
      dbSpan.end();
    }
  }

  private void saveSubscriptionToken(UUID subscriberId, String token) {
    SubscriptionToken subscriptionToken = new SubscriptionToken(token, subscriberId);

    tokenRepository.save(subscriptionToken);
  }

  private void sendConfirmationEmail(NewSubscriber newSubscriber, String token) {
    Span emailSpan = tracer.nextSpan().name("send-confirmation-email");
    emailSpan.tag("operation", "send-confirmation-email");
    emailSpan.tag("subscriber.email_hash", hashEmail(newSubscriber.getEmail().asString()));

    try (Tracer.SpanInScope ws = tracer.withSpan(emailSpan.start())) {
      String confirmationLink = "http://localhost:8080/subscriptions/confirm?token=" + token;

      String plainBody =
          "Welcome to our newsletter!\nVisit "
              + confirmationLink
              + " to confirm your subscription.";

      String htmlBody =
          "Welcome to our newsletter!<br/>"
              + "Click <a href=\""
              + confirmationLink
              + "\">here</a> to confirm your subscription.";

      emailClient.sendEmail(newSubscriber.getEmail(), "Welcome!", htmlBody, plainBody);
    } catch (Exception e) {
      emailSpan.error(e);
      log.error("Failed to send confirmation email", e);
      throw e;
    } finally {
      emailSpan.end();
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
