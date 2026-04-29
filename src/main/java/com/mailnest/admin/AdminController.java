package com.mailnest.admin;

import com.mailnest.session.SessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

  private final SessionService sessionService;

  public AdminController(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @GetMapping("/admin/dashboard")
  public ResponseEntity<String> dashboard(HttpSession session) {

    return sessionService
        .getUsername(session)
        .map(
            username -> {
              String html =
                  """
                            <!DOCTYPE html>
                            <html>
                            <body>
                                <p>Welcome %s!</p>
                            </body>
                            </html>
                            """
                      .formatted(username);

              return ResponseEntity.ok().body(html);
            })
        .orElseGet(() -> ResponseEntity.status(303).header(HttpHeaders.LOCATION, "/login").build());
  }
}
