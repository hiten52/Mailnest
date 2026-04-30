package com.mailnest.admin;

import com.mailnest.session.SessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LogoutController {

  private final SessionService sessionService;

  public LogoutController(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @PostMapping("/admin/logout")
  public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
    sessionService.logout(session);
    redirectAttributes.addFlashAttribute("error", "You have successfully logged out.");
    return "redirect:/login";
  }
}
