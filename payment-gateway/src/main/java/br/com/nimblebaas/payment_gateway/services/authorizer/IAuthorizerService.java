package br.com.nimblebaas.payment_gateway.services.authorizer;

import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.GetAuthorizerDTO;
import br.com.nimblebaas.payment_gateway.enums.authorizer.AuthorizerPurpose;

public interface IAuthorizerService {
    
    boolean isResponsible(AuthorizerPurpose authorizerPurpose);
    boolean authorize(GetAuthorizerDTO getAuthorizerDTO);
}
