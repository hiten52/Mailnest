package com.mailnest.login;

import com.mailnest.auth.AuthService;
import com.mailnest.config.HmacSecret;
import com.mailnest.error.UnauthorizedException;
import com.mailnest.newsletters.Credentials;
import com.mailnest.security.HmacUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

  private final AuthService authService;
  private final HmacSecret hmacSecret;

  public LoginController(AuthService authService, HmacSecret hmacSecret) {
    this.authService = authService;
    this.hmacSecret = hmacSecret;
  }

  @GetMapping("/login")
  public ResponseEntity<String> loginForm(
      @RequestParam(required = false) String error, @RequestParam(required = false) String tag)
      throws IOException {

    ClassPathResource resource = new ClassPathResource("templates/login.html");
    String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

    String errorHtml = "";

    if (error != null && tag != null) {

      String query = "error=" + URLEncoder.encode(error, StandardCharsets.UTF_8);

      if (HmacUtil.verify(query, tag, hmacSecret.getSecret())) {
        errorHtml = "<p><i>" + escapeHtml(error) + "</i></p>";
      } else {
        // tampered URL → ignore
      }
    }

    html = html.replace("{{error_message}}", errorHtml);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
        .body(html);
  }

  @PostMapping("/login")
  public ResponseEntity<Void> login(@Valid @ModelAttribute FormData form) {

    try {
      Credentials credentials = new Credentials(form.getUsername(), form.getPassword());

      authService.validateCredentials(credentials);

      return ResponseEntity.status(303).header(HttpHeaders.LOCATION, "/").build();

    } catch (UnauthorizedException e) {

      String error = "Authentication failed";

      String query = "error=" + URLEncoder.encode(error, StandardCharsets.UTF_8);

      String tag = HmacUtil.sign(query, hmacSecret.getSecret());

      return ResponseEntity.status(303)
          .header(HttpHeaders.LOCATION, "/login?" + query + "&tag=" + tag)
          .build();
    }
  }

  public static class FormData {

    @NotBlank private String username;

    @NotBlank private String password;

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }

  private String escapeHtml(String input) {
    return input
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}
