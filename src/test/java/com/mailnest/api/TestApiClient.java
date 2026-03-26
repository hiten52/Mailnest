package com.mailnest.api;

import com.mailnest.subscriptions.Subscriber;
import com.mailnest.subscriptions.SubscriberRepository;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class TestApiClient {

  private final String baseUrl;
  private final HttpClient client;
  private final SubscriberRepository subscriberRepository;

  public TestApiClient(int port, SubscriberRepository subscriberRepository) {
    this.baseUrl = "http://localhost:" + port;
    this.client = HttpClient.newHttpClient();
    this.subscriberRepository = subscriberRepository;
  }

  public HttpResponse<String> getHealthCheck() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(baseUrl + "/health_check")).GET().build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  public HttpResponse<String> postSubscriptions(String body)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/subscriptions"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  public void clearSubscribers() {
    subscriberRepository.deleteAll();
  }

  public List<Subscriber> getSavedSubscribers() {
    return subscriberRepository.findAll();
  }

  public String baseUrl() {
    return baseUrl;
  }
}
