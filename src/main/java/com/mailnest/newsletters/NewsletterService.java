package com.mailnest.newsletters;

import com.mailnest.domain.SubscriberEmail;
import com.mailnest.email.EmailClient;
import com.mailnest.subscriptions.SubscriberRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NewsletterService {

  private static final Logger log = LoggerFactory.getLogger(NewsletterService.class);

  private final SubscriberRepository repository;
  private final EmailClient emailClient;

  public NewsletterService(SubscriberRepository repository, EmailClient emailClient) {
    this.repository = repository;
    this.emailClient = emailClient;
  }

  public void publish(NewsletterRequest request) {

    List<String> emails = repository.findConfirmedSubscriberEmails();

    for (String rawEmail : emails) {

      var parsed = SubscriberEmail.parse(rawEmail);

      if (parsed.isEmpty()) {
        log.warn("Skipping confirmed subscriber with invalid stored email: {}", rawEmail);
        continue;
      }

      SubscriberEmail email = parsed.get();

      try {
        emailClient.sendEmail(
            email,
            request.getTitle(),
            request.getContent().getHtml(),
            request.getContent().getText());
      } catch (Exception e) {
        log.error("Failed to send newsletter to {}", email, e);
        throw e;
      }
    }
  }
}
