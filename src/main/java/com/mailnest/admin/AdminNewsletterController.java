package com.mailnest.admin;

import com.mailnest.newsletters.NewsletterRequest;
import com.mailnest.newsletters.NewsletterService;
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
public class AdminNewsletterController {

  private final NewsletterService newsletterService;

  public AdminNewsletterController(NewsletterService newsletterService) {
    this.newsletterService = newsletterService;
  }

  @GetMapping("/admin/newsletters")
  public ResponseEntity<String> newsletterForm(@ModelAttribute("error") String error)
      throws IOException {

    ClassPathResource resource = new ClassPathResource("templates/newsletter.html");
    String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

    String errorHtml = "";
    if (error != null && !error.isBlank()) {
      errorHtml = "<p><i>" + escapeHtml(error) + "</i></p>";
    }

    return ResponseEntity.ok().body(html.replace("{{error_message}}", errorHtml));
  }

  @PostMapping("/admin/newsletters")
  public String publishNewsletter(
      @ModelAttribute NewsletterFormData form, RedirectAttributes redirectAttributes) {

    if (form.getTitle() == null || form.getTitle().isBlank()) {
      redirectAttributes.addFlashAttribute("error", "Title is required.");
      return "redirect:/admin/newsletters";
    }

    if ((form.getTextContent() == null || form.getTextContent().isBlank())
        && (form.getHtmlContent() == null || form.getHtmlContent().isBlank())) {
      redirectAttributes.addFlashAttribute("error", "Content is required.");
      return "redirect:/admin/newsletters";
    }

    NewsletterRequest request = new NewsletterRequest();
    request.setTitle(form.getTitle());

    NewsletterRequest.Content content = new NewsletterRequest.Content();
    content.setText(form.getTextContent() != null ? form.getTextContent() : "");
    content.setHtml(form.getHtmlContent() != null ? form.getHtmlContent() : "");
    request.setContent(content);

    newsletterService.publish(request);

    redirectAttributes.addFlashAttribute("error", "Newsletter published successfully!");
    return "redirect:/admin/newsletters";
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
