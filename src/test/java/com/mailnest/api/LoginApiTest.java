package com.mailnest.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.mailnest.newsletters.UserRepository;
import com.mailnest.subscriptions.SubscriberRepository;
import com.mailnest.subscriptions.SubscriptionTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoginApiTest {

  @LocalServerPort private int port;

  @Autowired private SubscriberRepository subscriberRepository;

  @Autowired private SubscriptionTokenRepository tokenRepository;

  @Autowired private UserRepository userRepository;

  private TestApiClient api;

  @BeforeEach
  void setUp() {
    api = new TestApiClient(port, subscriberRepository, tokenRepository, userRepository);
    api.clearSubscribers();
  }

  @Test
  void anErrorFlashMessageIsSetOnFailure() throws Exception {
    var response = api.postLogin("username=wrong&password=wrong");

    assertThat(response.statusCode()).isEqualTo(200);

    String html = response.body();

    assertThat(html).contains("<p><i>Authentication failed</i></p>");

    String secondPage = api.getLoginHtml();
    assertThat(secondPage).doesNotContain("Authentication failed");
  }

  @Test
  void redirect_to_admin_dashboard_after_login_success() throws Exception {
    String body = "username=test-user&password=test-password";

    api.postLogin(body);

    String html = api.getAdminDashboardHtml();

    assertThat(html).contains("Welcome test-user");
  }

  @Test
  void you_must_be_logged_in_to_access_dashboard() throws Exception {
    String html = api.getAdminDashboardHtml();

    assertThat(html).contains("<title>Login</title>");
  }
}
