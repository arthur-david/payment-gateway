package br.com.nimblebaas.payment_gateway.clients.configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.LoggerFactory;

import feign.Request;
import feign.Response;
import feign.Util;

public class CustomFeignLogger extends feign.Logger {

    private static final String EMPTY_BODY = "empty";
    private static final String ERROR_READING_BODY = "error reading body";

    private final org.slf4j.Logger logger;

    public CustomFeignLogger() {
        this.logger = LoggerFactory.getLogger(CustomFeignLogger.class);
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info("{}{}", methodTag(configKey), String.format(format, args));
        }
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if (logger.isInfoEnabled()) {
            String method = request.httpMethod().name();
            String url = request.url();
            String body = getRequestBody(request);

            logger.info("{} - {} - {}", method, url, body);
        }
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response,
            long elapsedTime) throws IOException {
        String method = response.request().httpMethod().name();
        int status = response.status();
        String url = response.request().url();
        
        byte[] bodyData = new byte[0];
        if (response.body() != null) {
            bodyData = Util.toByteArray(response.body().asInputStream());
        }
        
        String body = bodyData.length > 0 ? new String(bodyData, StandardCharsets.UTF_8) : EMPTY_BODY;

        if (logger.isInfoEnabled()) {
            logger.info("{} - {} - {} - {}", method, status, url, body);
        }

        return response.toBuilder()
                .body(bodyData)
                .build();
    }

    private String getRequestBody(Request request) {
        if (request.body() == null) {
            return EMPTY_BODY;
        }
        
        try {
            String body = new String(request.body(), StandardCharsets.UTF_8);
            return body.isEmpty() ? EMPTY_BODY : body;
        } catch (Exception e) {
            return ERROR_READING_BODY;
        }
    }
}

