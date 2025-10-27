package br.com.nimblebaas.payment_gateway.clients.authorizer.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizerResponseData {

    private String authorized;
}
