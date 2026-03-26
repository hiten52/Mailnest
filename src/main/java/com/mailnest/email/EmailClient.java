package com.mailnest.email;

import com.mailnest.domain.SubscriberEmail;

public interface EmailClient {
  void sendEmail(SubscriberEmail recipient, String subject, String htmlContent, String textContent);
}
