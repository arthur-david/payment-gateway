package br.com.nimblebaas.payment_gateway.events.authentication;

import br.com.nimblebaas.payment_gateway.entities.user.User;
import lombok.Getter;

@Getter
public class LoginSuccessEvent {
    
    private final User user;
    private final String cpfOrEmail;
    private final String requestInfo;

    public LoginSuccessEvent(User user, String cpfOrEmail, String requestInfo) {
        this.user = user;
        this.cpfOrEmail = cpfOrEmail;
        this.requestInfo = requestInfo;
    }
}

