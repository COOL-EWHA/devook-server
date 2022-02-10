package com.ewha.devookserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedHeaders("*")
        .allowedOriginPatterns("*")
        .exposedHeaders("Set-Cookie")
        .allowedMethods("POST", "GET", "PUT", "DELETE", "PATCH")
        .allowCredentials(true)
        .maxAge(3000);
  }
}
