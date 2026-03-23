package com.mailnest;

import org.springframework.boot.SpringApplication;

public class TestMailnestApplication {

  public static void main(String[] args) {
    SpringApplication.from(MailnestApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
