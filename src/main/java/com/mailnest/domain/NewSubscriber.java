package com.mailnest.domain;

public class NewSubscriber {

  private final SubscriberEmail email;
  private final SubscriberName name;

  public NewSubscriber(SubscriberEmail email, SubscriberName name) {
    this.email = email;
    this.name = name;
  }

  public SubscriberEmail getEmail() {
    return email;
  }

  public SubscriberName getName() {
    return name;
  }
}
