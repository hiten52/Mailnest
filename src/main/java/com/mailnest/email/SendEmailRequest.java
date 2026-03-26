package com.mailnest.email;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SendEmailRequest(
    @JsonProperty("From") String from,
    @JsonProperty("To") String to,
    @JsonProperty("Subject") String subject,
    @JsonProperty("HtmlBody") String htmlBody,
    @JsonProperty("TextBody") String textBody) {}
