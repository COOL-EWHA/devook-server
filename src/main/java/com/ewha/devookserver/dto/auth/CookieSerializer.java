package com.ewha.devookserver.dto.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.session.web.http.DefaultCookieSerializer;

public class CookieSerializer {

  @Bean
  public DefaultCookieSerializer cookieSerializer() {
    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setCookieName("Set-Cookie");
    serializer.setCookiePath("/");
    serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
    serializer.setDomainName("example.com");
    serializer.setSameSite(null);
    return serializer;
  }
}
