package br.com.nimblebaas.payment_gateway.services.authorizer;

import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.clients.authorizer.clients.AuthorizerApiClient;
import br.com.nimblebaas.payment_gateway.clients.authorizer.models.AuthorizerResponse;
import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.GetAuthorizerDTO;
import br.com.nimblebaas.payment_gateway.enums.authorizer.AuthorizerPurpose;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CardRefundAuthorizerService implements IAuthorizerService {

    private final AuthorizerApiClient authorizerApiClient;

    @Override
    public boolean isResponsible(AuthorizerPurpose authorizerPurpose) {
        return AuthorizerPurpose.CARD_REFUND.equals(authorizerPurpose);
    }

    @Override
    public boolean authorize(@Valid GetAuthorizerDTO getAuthorizerDTO) {
        try {
            AuthorizerResponse response = authorizerApiClient.authorizeCardRefund(
                getAuthorizerDTO.getCpf(),
                getAuthorizerDTO.getAmount(),
                getAuthorizerDTO.getIdentifier());
            return response.isAuthorized();
        } catch (Exception e) {
            throw new BusinessRuleException(getClass(), BusinessRules.AUTHORIZER_SERVICE_ERROR, e.getMessage());
        }
    }
}
