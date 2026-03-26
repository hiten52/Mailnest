package com.mailnest.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.mailnest.subscriptions.SubscriberRepository;
import com.mailnest.subscriptions.SubscriptionTokenRepository;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HealthCheckApiTest {

  @LocalServerPort private int port;

  @Autowired private SubscriberRepository subscriberRepository;
  @Autowired private SubscriptionTokenRepository tokenRepository;

  @Test
  void healthCheckWorks() throws IOException, InterruptedException {
    TestApiClient api = new TestApiClient(port, subscriberRepository, tokenRepository);

    var response = api.getHealthCheck();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEmpty();
  }
}
