package com.mailnest.admin;

import com.mailnest.auth.AuthService;
import com.mailnest.error.UnauthorizedException;
import com.mailnest.newsletters.Credentials;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordController {

  private final AuthService authService;

  public PasswordController(AuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/admin/password")
  public ResponseEntity<String> changePasswordForm(@ModelAttribute("error") String error)
      throws IOException {

    ClassPathResource resource = new ClassPathResource("templates/change_password.html");
    String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

    String errorHtml = "";
    if (error != null && !error.isBlank()) {
      errorHtml = "<p><i>" + escapeHtml(error) + "</i></p>";
    }

    return ResponseEntity.ok().body(html.replace("{{error_message}}", errorHtml));
  }

  @PostMapping("/admin/password")
  public String changePassword(
      @ModelAttribute ChangePasswordRequest form,
      HttpServletRequest request,
      RedirectAttributes redirectAttributes) {

    String username = (String) request.getAttribute("username");

    if (!form.getNewPassword().equals(form.getNewPasswordCheck())) {
      redirectAttributes.addFlashAttribute(
          "error", "You entered two different new passwords - the field values must match.");
      return "redirect:/admin/password";
    }

    if (form.getNewPassword().length() <= 12) {
      redirectAttributes.addFlashAttribute(
          "error", "The new password is too short - it must be longer than 12 characters.");
      return "redirect:/admin/password";
    }

    if (form.getNewPassword().length() >= 129) {
      redirectAttributes.addFlashAttribute(
          "error", "The new password is too long - it must be shorter than 129 characters.");
      return "redirect:/admin/password";
    }

    try {
      Credentials credentials = new Credentials(username, form.getCurrentPassword());
      authService.validateCredentials(credentials);
    } catch (UnauthorizedException e) {
      redirectAttributes.addFlashAttribute("error", "The current password is incorrect.");
      return "redirect:/admin/password";
    }

    authService.changePassword(username, form.getNewPassword());

    redirectAttributes.addFlashAttribute("error", "Your password has been changed.");
    return "redirect:/admin/password";
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
