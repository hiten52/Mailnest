package com.mailnest.health;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthCheckTest {

  @LocalServerPort private int port;

  @Test
  void healthCheckWorks() throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/health_check"))
            .GET()
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEmpty();
  }
}
