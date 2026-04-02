package com.mailnest.home;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

  @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> home() throws IOException {
    ClassPathResource resource = new ClassPathResource("templates/home.html");
    String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
        .body(html);
  }
}
