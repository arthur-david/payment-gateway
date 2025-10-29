package br.com.nimblebaas.payment_gateway.clients.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import feign.Logger;

class FeignClientConfigTest {

    private FeignClientConfig feignClientConfig;

    @BeforeEach
    void setUp() {
        feignClientConfig = new FeignClientConfig();
    }

    @Test
    void shouldCreateFeignLoggerLevel() {
        Logger.Level level = feignClientConfig.feignLoggerLevel();

        assertNotNull(level);
        assertEquals(Logger.Level.FULL, level);
    }

    @Test
    void shouldCreateCustomFeignLogger() {
        Logger logger = feignClientConfig.feignLogger();

        assertNotNull(logger);
        assertInstanceOf(CustomFeignLogger.class, logger);
    }
}

