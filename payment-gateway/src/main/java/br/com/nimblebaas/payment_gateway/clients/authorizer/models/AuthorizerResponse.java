package br.com.nimblebaas.payment_gateway.clients.authorizer.models;

import static java.util.Objects.nonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizerResponse {

    private String status;
    private AuthorizerResponseData data;

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }

    public boolean isAuthorized() {
        return isSuccess() && nonNull(data) && data.isAuthorized();
    }
}
