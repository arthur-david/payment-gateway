package br.com.nimblebaas.payment_gateway.clients.authorizer.models;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizerResponseData {

    private Boolean authorized;

    public boolean isAuthorized() {
        return isTrue(authorized);
    }
}
