package br.com.nimblebaas.payment_gateway.services.authorizer;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.clients.authorizer.clients.AuthorizerApiClient;
import br.com.nimblebaas.payment_gateway.clients.authorizer.models.AuthorizerResponse;
import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.AuthorizerCardDetailsDTO;
import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.GetAuthorizerDTO;
import br.com.nimblebaas.payment_gateway.enums.authorizer.AuthorizerPurpose;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CardPaymentAuthorizerService implements IAuthorizerService {

    private final AuthorizerApiClient authorizerApiClient;
    
    @Override
    public boolean isResponsible(AuthorizerPurpose authorizerPurpose) {
        return AuthorizerPurpose.CARD_PAYMENT.equals(authorizerPurpose);
    }

    @Override
    public boolean authorize(@Valid GetAuthorizerDTO getAuthorizerDTO) {
        validateCardDetails(getAuthorizerDTO.getCardDetails());
        
        try {
            AuthorizerResponse response = authorizerApiClient.authorizeCardPayment(
                getAuthorizerDTO.getCpf(),
                getAuthorizerDTO.getAmount(),
                getAuthorizerDTO.getCardDetails().getInstallments(),
                getAuthorizerDTO.getCardDetails().getCardNumber(),
                getAuthorizerDTO.getCardDetails().getCardCvv(),
                getAuthorizerDTO.getCardDetails().getCardExpirationDate(),
                getAuthorizerDTO.getIdentifier());
            return response.isAuthorized();
        } catch (Exception e) {
            throw new BusinessRuleException(getClass(), BusinessRules.AUTHORIZER_SERVICE_ERROR, e.getMessage());
        }
    }

    private void validateCardDetails(AuthorizerCardDetailsDTO cardDetails) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<AuthorizerCardDetailsDTO>> violations = validator.validate(cardDetails);
        if (!violations.isEmpty()) {
            throw new BusinessRuleException(
                getClass(), 
                BusinessRules.INVALID_INPUT_DATA, 
                "Erros de validação dos dados do cartão: %s", 
                violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", ")));
        }
    }
}
