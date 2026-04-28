package com.mailnest.login;

import com.mailnest.auth.AuthService;
import com.mailnest.error.UnauthorizedException;
import com.mailnest.newsletters.Credentials;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

  private final AuthService authService;

  public LoginController(AuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/login")
  @ResponseBody
  public String loginForm(@ModelAttribute("error") String error) throws IOException {

    ClassPathResource resource = new ClassPathResource("templates/login.html");
    String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

    String errorHtml = "";

    if (error != null && !error.isBlank()) {
      errorHtml = "<p><i>" + escapeHtml(error) + "</i></p>";
    }

    return html.replace("{{error_message}}", errorHtml);
  }

  @PostMapping("/login")
  public String login(
      @Valid @ModelAttribute FormData form,
      org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

    try {
      Credentials credentials = new Credentials(form.getUsername(), form.getPassword());
      authService.validateCredentials(credentials);

      return "redirect:/";

    } catch (UnauthorizedException e) {
      redirectAttributes.addFlashAttribute("error", "Authentication failed");
      return "redirect:/login";
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
