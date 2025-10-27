package br.com.nimblebaas.payment_gateway.clients.configuration;

import org.springframework.context.annotation.Bean;

import feign.Logger;

public class FeignClientConfig {
    
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
