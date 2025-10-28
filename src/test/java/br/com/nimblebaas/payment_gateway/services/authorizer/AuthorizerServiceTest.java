package br.com.nimblebaas.payment_gateway.services.authorizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.GetAuthorizerDTO;
import br.com.nimblebaas.payment_gateway.enums.authorizer.AuthorizerPurpose;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;

@ExtendWith(MockitoExtension.class)
class AuthorizerServiceTest {

    @Mock
    private IAuthorizerService depositAuthorizerService;

    @Mock
    private IAuthorizerService cardPaymentAuthorizerService;

    private AuthorizerService authorizerService;

    private GetAuthorizerDTO getAuthorizerDTO;

    @BeforeEach
    void setUp() {
        List<IAuthorizerService> authorizerServices = Arrays.asList(
            depositAuthorizerService,
            cardPaymentAuthorizerService
        );
        authorizerService = new AuthorizerService(authorizerServices);

        getAuthorizerDTO = GetAuthorizerDTO.builder()
            .cpf("12345678900")
            .amount(new BigDecimal("100.00"))
            .identifier("test-identifier")
            .build();
    }

    @Test
    void authorize_WithDepositPurpose_ShouldReturnTrue() {
        when(depositAuthorizerService.isResponsible(AuthorizerPurpose.DEPOSIT)).thenReturn(true);
        when(depositAuthorizerService.authorize(any(GetAuthorizerDTO.class))).thenReturn(true);

        boolean result = authorizerService.authorize(AuthorizerPurpose.DEPOSIT, getAuthorizerDTO);

        assertTrue(result);
    }

    @Test
    void authorize_WithCardPaymentPurpose_ShouldReturnTrue() {
        when(cardPaymentAuthorizerService.isResponsible(AuthorizerPurpose.CARD_PAYMENT)).thenReturn(true);
        when(cardPaymentAuthorizerService.authorize(any(GetAuthorizerDTO.class))).thenReturn(true);

        boolean result = authorizerService.authorize(AuthorizerPurpose.CARD_PAYMENT, getAuthorizerDTO);

        assertTrue(result);
    }

    @Test
    void authorize_WithUnknownPurpose_ShouldThrowException() {
        when(depositAuthorizerService.isResponsible(any())).thenReturn(false);
        when(cardPaymentAuthorizerService.isResponsible(any())).thenReturn(false);

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> authorizerService.authorize(AuthorizerPurpose.CARD_REFUND, getAuthorizerDTO)
        );

        assertEquals(BusinessRules.AUTHORIZER_SERVICE_NOT_FOUND.name(), exception.getErrorDTO().getReason());
    }
}

