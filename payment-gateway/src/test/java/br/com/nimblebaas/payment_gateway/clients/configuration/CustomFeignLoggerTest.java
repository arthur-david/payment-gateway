package br.com.nimblebaas.payment_gateway.clients.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import feign.Request;
import feign.Response;

class CustomFeignLoggerTest {

    private CustomFeignLogger customFeignLogger;

    @BeforeEach
    void setUp() {
        customFeignLogger = new CustomFeignLogger();
    }

    @Test
    void shouldCreateLogger() {
        assertNotNull(customFeignLogger);
    }

    @Test
    void shouldLogRequest() {
        Map<String, Collection<String>> headers = new HashMap<>();
        byte[] body = "test body".getBytes(StandardCharsets.UTF_8);
        
        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://localhost:8080/api/test",
            headers,
            body,
            StandardCharsets.UTF_8,
            null
        );

        customFeignLogger.logRequest("TestClient#method()", feign.Logger.Level.FULL, request);
        
        assertNotNull(request);
    }

    @Test
    void shouldLogRequestWithNullBody() {
        Map<String, Collection<String>> headers = new HashMap<>();
        
        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://localhost:8080/api/test",
            headers,
            null,
            StandardCharsets.UTF_8,
            null
        );

        customFeignLogger.logRequest("TestClient#method()", feign.Logger.Level.FULL, request);
        
        assertNotNull(request);
    }

    @Test
    void shouldLogRequestWithEmptyBody() {
        Map<String, Collection<String>> headers = new HashMap<>();
        byte[] body = "".getBytes(StandardCharsets.UTF_8);
        
        Request request = Request.create(
            Request.HttpMethod.POST,
            "http://localhost:8080/api/test",
            headers,
            body,
            StandardCharsets.UTF_8,
            null
        );

        customFeignLogger.logRequest("TestClient#method()", feign.Logger.Level.FULL, request);
        
        assertNotNull(request);
    }

    @Test
    void shouldLogAndRebufferResponse() throws IOException {
        Map<String, Collection<String>> headers = new HashMap<>();
        byte[] requestBody = "request".getBytes(StandardCharsets.UTF_8);
        
        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://localhost:8080/api/test",
            headers,
            requestBody,
            StandardCharsets.UTF_8,
            null
        );

        String responseBody = "{\"status\":\"success\"}";
        Response response = Response.builder()
            .status(200)
            .reason("OK")
            .request(request)
            .headers(headers)
            .body(responseBody, StandardCharsets.UTF_8)
            .build();

        Response result = customFeignLogger.logAndRebufferResponse(
            "TestClient#method()", 
            feign.Logger.Level.FULL, 
            response, 
            100L
        );

        assertNotNull(result);
        assertEquals(200, result.status());
    }

    @Test
    void shouldLogAndRebufferResponseWithNullBody() throws IOException {
        Map<String, Collection<String>> headers = new HashMap<>();
        byte[] requestBody = "request".getBytes(StandardCharsets.UTF_8);
        
        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://localhost:8080/api/test",
            headers,
            requestBody,
            StandardCharsets.UTF_8,
            null
        );

        Response response = Response.builder()
            .status(204)
            .reason("No Content")
            .request(request)
            .headers(headers)
            .build();

        Response result = customFeignLogger.logAndRebufferResponse(
            "TestClient#method()", 
            feign.Logger.Level.FULL, 
            response, 
            100L
        );

        assertNotNull(result);
        assertEquals(204, result.status());
    }

    @Test
    void shouldLogAndRebufferResponseWithEmptyBody() throws IOException {
        Map<String, Collection<String>> headers = new HashMap<>();
        byte[] requestBody = "request".getBytes(StandardCharsets.UTF_8);
        
        Request request = Request.create(
            Request.HttpMethod.DELETE,
            "http://localhost:8080/api/test",
            headers,
            requestBody,
            StandardCharsets.UTF_8,
            null
        );

        Response response = Response.builder()
            .status(200)
            .reason("OK")
            .request(request)
            .headers(headers)
            .body("", StandardCharsets.UTF_8)
            .build();

        Response result = customFeignLogger.logAndRebufferResponse(
            "TestClient#method()", 
            feign.Logger.Level.FULL, 
            response, 
            100L
        );

        assertNotNull(result);
        assertEquals(200, result.status());
    }
}

