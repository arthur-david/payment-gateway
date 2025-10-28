package br.com.nimblebaas.payment_gateway.events.authentication;

import lombok.Getter;

@Getter
public class LoginFailureEvent {
    
    private final String cpfOrEmail;
    private final String reason;
    private final String requestInfo;
    private final Exception exception;

    public LoginFailureEvent(String cpfOrEmail, String reason, String requestInfo, Exception exception) {
        this.cpfOrEmail = cpfOrEmail;
        this.reason = reason;
        this.requestInfo = requestInfo;
        this.exception = exception;
    }
}

