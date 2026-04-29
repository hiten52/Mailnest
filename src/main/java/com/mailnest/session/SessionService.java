package com.mailnest.session;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SessionService {

  private static final String USERNAME_KEY = "username";

  public void login(HttpSession session, String username) {
    session.setAttribute(USERNAME_KEY, username);
  }

  public Optional<String> getUsername(HttpSession session) {
    Object value = session.getAttribute(USERNAME_KEY);
    return Optional.ofNullable((String) value);
  }

  public HttpSession renewSession(
      HttpSession oldSession, jakarta.servlet.http.HttpServletRequest request) {
    if (oldSession != null) {
      oldSession.invalidate();
    }
    return request.getSession(true);
  }

  public void logout(HttpSession session) {
    session.invalidate();
  }
}
