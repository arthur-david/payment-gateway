package br.com.nimblebaas.payment_gateway.services.authorizer;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.GetAuthorizerDTO;
import br.com.nimblebaas.payment_gateway.enums.authorizer.AuthorizerPurpose;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class AuthorizerService {

    private final List<IAuthorizerService> authorizerServices;

    private IAuthorizerService getAuthorizerService(AuthorizerPurpose authorizerPurpose) {
        return authorizerServices.stream()
            .filter(service -> service.isResponsible(authorizerPurpose))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleException(
                getClass(), 
                BusinessRules.AUTHORIZER_SERVICE_NOT_FOUND, 
                "No authorizer service found for purpose: %s", 
                authorizerPurpose.name())
            );
    }

    public boolean authorize(AuthorizerPurpose authorizerPurpose, GetAuthorizerDTO getAuthorizerDTO) {
        var authorizerService = getAuthorizerService(authorizerPurpose);
        return authorizerService.authorize(getAuthorizerDTO);
    }
}
