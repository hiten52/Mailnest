package com.mailnest.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

  @GetMapping("/admin/dashboard")
  public ResponseEntity<String> dashboard(HttpServletRequest request) {

    String username = (String) request.getAttribute("username");

    String html =
        """
        <!DOCTYPE html>
        <html>
        <body>
            <p>Welcome %s!</p>
            <p>Available actions:</p>
            <ol>
                <li><a href="/admin/password">Change password</a></li>
                <li><a href="/admin/newsletters">Send a newsletter issue</a></li>
                <li>
                    <form name="logoutForm" action="/admin/logout" method="post">
                        <input type="submit" value="Logout">
                    </form>
                </li>
            </ol>
        </body>
        </html>
        """
            .formatted(username);

    return ResponseEntity.ok().body(html);
  }
}
