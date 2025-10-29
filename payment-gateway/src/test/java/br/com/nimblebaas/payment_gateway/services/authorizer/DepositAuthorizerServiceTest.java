package br.com.nimblebaas.payment_gateway.services.authorizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.nimblebaas.payment_gateway.clients.authorizer.clients.AuthorizerApiClient;
import br.com.nimblebaas.payment_gateway.clients.authorizer.models.AuthorizerResponse;
import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.GetAuthorizerDTO;
import br.com.nimblebaas.payment_gateway.enums.authorizer.AuthorizerPurpose;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;

@ExtendWith(MockitoExtension.class)
class DepositAuthorizerServiceTest {

    @Mock
    private AuthorizerApiClient authorizerApiClient;

    @InjectMocks
    private DepositAuthorizerService depositAuthorizerService;

    private GetAuthorizerDTO getAuthorizerDTO;

    @BeforeEach
    void setUp() {
        getAuthorizerDTO = GetAuthorizerDTO.builder()
            .cpf("12345678900")
            .amount(new BigDecimal("100.00"))
            .identifier("test-identifier")
            .build();
    }

    @Test
    void isResponsible_WithDepositPurpose_ShouldReturnTrue() {
        boolean result = depositAuthorizerService.isResponsible(AuthorizerPurpose.DEPOSIT);

        assertTrue(result);
    }

    @Test
    void isResponsible_WithOtherPurpose_ShouldReturnFalse() {
        boolean result = depositAuthorizerService.isResponsible(AuthorizerPurpose.CARD_PAYMENT);

        assertFalse(result);
    }

    @Test
    void authorize_WithValidData_ShouldReturnTrue() {
        AuthorizerResponse authorizerResponse = new AuthorizerResponse();
        authorizerResponse.setStatus("success");
        authorizerResponse.setData(new br.com.nimblebaas.payment_gateway.clients.authorizer.models.AuthorizerResponseData(true));
        when(authorizerApiClient.authorizeDeposit(
            anyString(),
            any(BigDecimal.class),
            anyString()
        )).thenReturn(authorizerResponse);

        boolean result = depositAuthorizerService.authorize(getAuthorizerDTO);

        assertTrue(result);
    }

    @Test
    void authorize_WithUnauthorizedResponse_ShouldReturnFalse() {
        AuthorizerResponse authorizerResponse = new AuthorizerResponse();
        authorizerResponse.setStatus("success");
        authorizerResponse.setData(new br.com.nimblebaas.payment_gateway.clients.authorizer.models.AuthorizerResponseData(false));
        when(authorizerApiClient.authorizeDeposit(
            anyString(),
            any(BigDecimal.class),
            anyString()
        )).thenReturn(authorizerResponse);

        boolean result = depositAuthorizerService.authorize(getAuthorizerDTO);

        assertFalse(result);
    }

    @Test
    void authorize_WithApiException_ShouldThrowBusinessRuleException() {
        when(authorizerApiClient.authorizeDeposit(
            anyString(),
            any(BigDecimal.class),
            anyString()
        )).thenThrow(new RuntimeException("API Error"));

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> depositAuthorizerService.authorize(getAuthorizerDTO)
        );

        assertEquals(BusinessRules.AUTHORIZER_SERVICE_ERROR.name(), exception.getErrorDTO().getReason());
    }
}

