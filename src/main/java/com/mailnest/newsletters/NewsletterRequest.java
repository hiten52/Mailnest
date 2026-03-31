package com.mailnest.newsletters;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;

@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP"},
    justification = "DTO used only for deserialization; mutability is acceptable")
public class NewsletterRequest {

  @NotBlank private String title;

  @Valid @NotNull private Content content;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Content getContent() {
    return content;
  }

  public void setContent(Content content) {
    this.content = content;
  }

  public static class Content {
    @NotBlank private String text;

    @NotBlank private String html;

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }

    public String getHtml() {
      return html;
    }

    public void setHtml(String html) {
      this.html = html;
    }
  }
}
