package com.mailnest.config;

import com.mailnest.admin.RejectAnonymousUsersInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final RejectAnonymousUsersInterceptor rejectAnonymousUsersInterceptor;

  public WebMvcConfig(RejectAnonymousUsersInterceptor rejectAnonymousUsersInterceptor) {
    this.rejectAnonymousUsersInterceptor = rejectAnonymousUsersInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rejectAnonymousUsersInterceptor).addPathPatterns("/admin/**");
  }
}
