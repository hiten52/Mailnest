package com.mailnest.api;

import com.mailnest.newsletters.User;
import com.mailnest.newsletters.UserRepository;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class TestUser {

  public final UUID userId;
  public final String username;
  public final String password;

  private TestUser(UUID userId, String username, String password) {
    this.userId = userId;
    this.username = username;
    this.password = password;
  }

  public static TestUser create() {
    return new TestUser(
        UUID.randomUUID(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }

  public void save(UserRepository repo) {
    User user = new User();
    user.setUserId(userId);
    user.setUsername(username);
    user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
    repo.save(user);
  }
}
