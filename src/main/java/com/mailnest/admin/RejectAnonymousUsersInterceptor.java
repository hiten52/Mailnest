package com.mailnest.admin;

import com.mailnest.session.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RejectAnonymousUsersInterceptor implements HandlerInterceptor {

  private final SessionService sessionService;

  public RejectAnonymousUsersInterceptor(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    var username = sessionService.getUsername(request.getSession());
    if (username.isEmpty()) {
      response.setStatus(303);
      response.setHeader("Location", "/login");
      return false;
    }

    // Store the username as a request attribute so controllers can access it
    // without needing to query the session again
    request.setAttribute("username", username.get());
    return true;
  }
}
