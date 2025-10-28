package br.com.nimblebaas.payment_gateway.services.authorizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.AuthorizerCardDetailsDTO;
import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.GetAuthorizerDTO;
import br.com.nimblebaas.payment_gateway.enums.authorizer.AuthorizerPurpose;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;

@ExtendWith(MockitoExtension.class)
class CardPaymentAuthorizerServiceTest {

    @Mock
    private AuthorizerApiClient authorizerApiClient;

    @InjectMocks
    private CardPaymentAuthorizerService cardPaymentAuthorizerService;

    private GetAuthorizerDTO getAuthorizerDTO;

    @BeforeEach
    void setUp() {
        AuthorizerCardDetailsDTO cardDetails = AuthorizerCardDetailsDTO.builder()
            .cardNumber("1234567890123456")
            .cardCvv("123")
            .cardExpirationDate("12/2025")
            .installments(1)
            .build();

        getAuthorizerDTO = GetAuthorizerDTO.builder()
            .cpf("12345678900")
            .amount(new BigDecimal("100.00"))
            .identifier("test-identifier")
            .cardDetails(cardDetails)
            .build();
    }

    @Test
    void isResponsible_WithCardPaymentPurpose_ShouldReturnTrue() {
        boolean result = cardPaymentAuthorizerService.isResponsible(AuthorizerPurpose.CARD_PAYMENT);

        assertTrue(result);
    }

    @Test
    void isResponsible_WithOtherPurpose_ShouldReturnFalse() {
        boolean result = cardPaymentAuthorizerService.isResponsible(AuthorizerPurpose.DEPOSIT);

        assertFalse(result);
    }

    @Test
    void authorize_WithValidData_ShouldReturnTrue() {
        AuthorizerResponse authorizerResponse = new AuthorizerResponse();
        authorizerResponse.setStatus("success");
        authorizerResponse.setData(new br.com.nimblebaas.payment_gateway.clients.authorizer.models.AuthorizerResponseData(true));
        when(authorizerApiClient.authorizeCardPayment(
            anyString(),
            any(BigDecimal.class),
            anyInt(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(authorizerResponse);

        boolean result = cardPaymentAuthorizerService.authorize(getAuthorizerDTO);

        assertTrue(result);
    }

    @Test
    void authorize_WithUnauthorizedResponse_ShouldReturnFalse() {
        AuthorizerResponse authorizerResponse = new AuthorizerResponse();
        authorizerResponse.setStatus("success");
        authorizerResponse.setData(new br.com.nimblebaas.payment_gateway.clients.authorizer.models.AuthorizerResponseData(false));
        when(authorizerApiClient.authorizeCardPayment(
            anyString(),
            any(BigDecimal.class),
            anyInt(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(authorizerResponse);

        boolean result = cardPaymentAuthorizerService.authorize(getAuthorizerDTO);

        assertFalse(result);
    }

    @Test
    void authorize_WithApiException_ShouldThrowBusinessRuleException() {
        when(authorizerApiClient.authorizeCardPayment(
            anyString(),
            any(BigDecimal.class),
            anyInt(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
        )).thenThrow(new RuntimeException("API Error"));

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> cardPaymentAuthorizerService.authorize(getAuthorizerDTO)
        );

        assertEquals(BusinessRules.AUTHORIZER_SERVICE_ERROR.name(), exception.getErrorDTO().getReason());
    }
}

