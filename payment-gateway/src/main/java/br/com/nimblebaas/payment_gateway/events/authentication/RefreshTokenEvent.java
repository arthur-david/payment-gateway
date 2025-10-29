package br.com.nimblebaas.payment_gateway.events.authentication;

import br.com.nimblebaas.payment_gateway.entities.user.User;
import lombok.Getter;

@Getter
public class RefreshTokenEvent {
    
    private final User user;
    private final String cpfOrEmail;
    private final String requestInfo;
    private final boolean success;
    private final String message;

    public RefreshTokenEvent(User user, String cpfOrEmail, String requestInfo, boolean success, String message) {
        this.user = user;
        this.cpfOrEmail = cpfOrEmail;
        this.requestInfo = requestInfo;
        this.success = success;
        this.message = message;
    }
}

