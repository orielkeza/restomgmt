package com.restomgmt.site.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mtn.momo")
public class MomoConfig {
    private String baseUrl;
    private String subscriptionKey;
    private String apiUser;
    private String apiKey;
    private String environment;
    private String currency;
}