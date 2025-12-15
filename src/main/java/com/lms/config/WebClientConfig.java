package com.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${rustici.base-url}")
    private String baseUrl;

    @Value("${rustici.app-id}")
    private String appId;

    @Value("${rustici.secret}")
    private String secret;

    @Bean
    public WebClient rusticiWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> headers.setBasicAuth(appId, secret))
                .build();
    }
}
