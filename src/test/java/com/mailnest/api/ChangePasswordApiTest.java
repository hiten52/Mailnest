package com.mailnest.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.mailnest.newsletters.UserRepository;
import com.mailnest.subscriptions.SubscriberRepository;
import com.mailnest.subscriptions.SubscriptionTokenRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChangePasswordApiTest {

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
  void you_must_be_logged_in_to_see_the_change_password_form() throws Exception {
    String html = api.getChangePasswordHtml();

    assertThat(html).contains("<title>Login</title>");
  }

  @Test
  void you_must_be_logged_in_to_change_your_password() throws Exception {
    String newPassword = UUID.randomUUID().toString();

    String body =
        "currentPassword="
            + encode(UUID.randomUUID().toString())
            + "&newPassword="
            + encode(newPassword)
            + "&newPasswordCheck="
            + encode(newPassword);

    var response = api.postChangePassword(body);

    assertThat(response.body()).contains("<title>Login</title>");
  }

  @Test
  void new_password_fields_must_match() throws Exception {
    String newPassword = UUID.randomUUID().toString();
    String anotherNewPassword = UUID.randomUUID().toString();

    api.postLogin(
        "username=" + encode(api.testUser.username) + "&password=" + encode(api.testUser.password));

    String body =
        "currentPassword="
            + encode(api.testUser.password)
            + "&newPassword="
            + encode(newPassword)
            + "&newPasswordCheck="
            + encode(anotherNewPassword);

    var response = api.postChangePassword(body);

    assertThat(response.body())
        .contains("You entered two different new passwords - the field values must match.");
  }

  @Test
  void current_password_must_be_valid() throws Exception {
    String newPassword = UUID.randomUUID().toString() + UUID.randomUUID();
    String wrongPassword = UUID.randomUUID().toString();

    api.postLogin(
        "username=" + encode(api.testUser.username) + "&password=" + encode(api.testUser.password));

    String body =
        "currentPassword="
            + encode(wrongPassword)
            + "&newPassword="
            + encode(newPassword)
            + "&newPasswordCheck="
            + encode(newPassword);

    var response = api.postChangePassword(body);

    assertThat(response.body()).contains("The current password is incorrect.");
  }

  @Test
  void new_password_must_be_longer_than_12_characters() throws Exception {
    String shortPassword = "short";

    api.postLogin(
        "username=" + encode(api.testUser.username) + "&password=" + encode(api.testUser.password));

    String body =
        "currentPassword="
            + encode(api.testUser.password)
            + "&newPassword="
            + encode(shortPassword)
            + "&newPasswordCheck="
            + encode(shortPassword);

    var response = api.postChangePassword(body);

    assertThat(response.body())
        .contains("The new password is too short - it must be longer than 12 characters.");
  }

  @Test
  void new_password_must_be_shorter_than_129_characters() throws Exception {
    String longPassword = "a".repeat(129);

    api.postLogin(
        "username=" + encode(api.testUser.username) + "&password=" + encode(api.testUser.password));

    String body =
        "currentPassword="
            + encode(api.testUser.password)
            + "&newPassword="
            + encode(longPassword)
            + "&newPasswordCheck="
            + encode(longPassword);

    var response = api.postChangePassword(body);

    assertThat(response.body())
        .contains("The new password is too long - it must be shorter than 129 characters.");
  }

  @Test
  void changing_password_works() throws Exception {
    String newPassword = UUID.randomUUID().toString() + UUID.randomUUID();

    api.postLogin(
        "username=" + encode(api.testUser.username) + "&password=" + encode(api.testUser.password));

    String body =
        "currentPassword="
            + encode(api.testUser.password)
            + "&newPassword="
            + encode(newPassword)
            + "&newPasswordCheck="
            + encode(newPassword);

    var response = api.postChangePassword(body);

    assertThat(response.body()).contains("Your password has been changed.");

    var logoutResponse = api.postLogout();

    assertThat(logoutResponse.body()).contains("You have successfully logged out.");

    api.postLogin("username=" + encode(api.testUser.username) + "&password=" + encode(newPassword));

    String html = api.getAdminDashboardHtml();

    assertThat(html).contains("Welcome " + api.testUser.username);
  }

  @Test
  void logout_clears_session_state() throws Exception {
    api.postLogin(
        "username=" + encode(api.testUser.username) + "&password=" + encode(api.testUser.password));

    String html = api.getAdminDashboardHtml();
    assertThat(html).contains("Welcome " + api.testUser.username);

    var logoutResponse = api.postLogout();

    assertThat(logoutResponse.body()).contains("You have successfully logged out.");

    String dashboardHtml = api.getAdminDashboardHtml();

    assertThat(dashboardHtml).contains("<title>Login</title>");
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
