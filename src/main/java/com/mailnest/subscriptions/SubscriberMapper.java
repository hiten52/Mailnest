package com.mailnest.subscriptions;

import com.mailnest.domain.NewSubscriber;
import com.mailnest.domain.SubscriberEmail;
import com.mailnest.domain.SubscriberName;
import com.mailnest.subscriptions.error.InvalidSubscriberException;

public class SubscriberMapper {

  public static NewSubscriber from(SubscriptionRequest request) {

    var name =
        SubscriberName.parse(request.getName())
            .orElseThrow(() -> new InvalidSubscriberException("Invalid name"));

    var email =
        SubscriberEmail.parse(request.getEmail())
            .orElseThrow(() -> new InvalidSubscriberException("Invalid email"));

    return new NewSubscriber(email, name);
  }
}
