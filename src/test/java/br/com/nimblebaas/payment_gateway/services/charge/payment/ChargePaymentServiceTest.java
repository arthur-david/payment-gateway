package br.com.nimblebaas.payment_gateway.services.charge.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargePaymentInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.internal.charge.ChargePaymentDTO;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.entities.charge.ChargePayment;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;
import br.com.nimblebaas.payment_gateway.enums.charge.PaymentMethod;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;

@ExtendWith(MockitoExtension.class)
class ChargePaymentServiceTest {

    @Mock
    private IChargePaymentService balanceChargePaymentService;

    @Mock
    private IChargePaymentService cardChargePaymentService;

    private ChargePaymentService chargePaymentService;

    private ChargePaymentInputRecord chargePaymentInputRecord;
    private Charge charge;

    @BeforeEach
    void setUp() {
        List<IChargePaymentService> chargePaymentServices = Arrays.asList(
            balanceChargePaymentService,
            cardChargePaymentService
        );
        chargePaymentService = new ChargePaymentService(chargePaymentServices);

        chargePaymentInputRecord = new ChargePaymentInputRecord(
            "charge-identifier",
            PaymentMethod.ACCOUNT_BALANCE,
            null, null, null, null
        );

        charge = Charge.builder()
            .identifier("charge-identifier")
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PENDING)
            .build();
    }

    @Test
    void pay_WithBalancePaymentMethod_ShouldCallBalanceService() {
        ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargePaymentInputRecord, charge);

        when(balanceChargePaymentService.isResponsible(PaymentMethod.ACCOUNT_BALANCE)).thenReturn(true);

        chargePaymentService.pay(chargePaymentDTO, PaymentMethod.ACCOUNT_BALANCE);

        verify(balanceChargePaymentService).pay(any(ChargePaymentDTO.class));
    }

    @Test
    void pay_WithCreditCardPaymentMethod_ShouldCallCardService() {
        ChargePaymentInputRecord cardPaymentInput = new ChargePaymentInputRecord(
            "charge-identifier",
            PaymentMethod.CREDIT_CARD,
            "1234567890123456",
            "123",
            "12/2025",
            1
        );
        ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(cardPaymentInput, charge);

        when(cardChargePaymentService.isResponsible(PaymentMethod.CREDIT_CARD)).thenReturn(true);

        chargePaymentService.pay(chargePaymentDTO, PaymentMethod.CREDIT_CARD);

        verify(cardChargePaymentService).pay(any(ChargePaymentDTO.class));
    }

    @Test
    void pay_WithUnknownPaymentMethod_ShouldThrowException() {
        ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargePaymentInputRecord, charge);

        when(balanceChargePaymentService.isResponsible(any())).thenReturn(false);
        when(cardChargePaymentService.isResponsible(any())).thenReturn(false);

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> chargePaymentService.pay(chargePaymentDTO, PaymentMethod.ACCOUNT_BALANCE)
        );

        assertEquals(BusinessRules.CHARGE_PAYMENT_SERVICE_NOT_FOUND.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void cancel_WithBalancePayment_ShouldCallBalanceService() {
        ChargePayment payment = ChargePayment.builder()
            .charge(charge)
            .paymentMethod(PaymentMethod.ACCOUNT_BALANCE)
            .build();
        charge.setPayment(payment);

        when(balanceChargePaymentService.isResponsible(PaymentMethod.ACCOUNT_BALANCE)).thenReturn(true);

        chargePaymentService.cancel(charge);

        verify(balanceChargePaymentService).cancel(charge);
    }
}

