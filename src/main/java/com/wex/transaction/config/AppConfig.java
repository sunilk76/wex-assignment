package com.wex.transaction.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
    	ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
    			.withConnectTimeout(Duration.ofSeconds(5))
    			.withReadTimeout(Duration.ofSeconds(10));
    	return new RestTemplate(ClientHttpRequestFactories.get(settings));
    }
}
