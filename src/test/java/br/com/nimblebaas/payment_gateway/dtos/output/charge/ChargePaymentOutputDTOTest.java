package br.com.nimblebaas.payment_gateway.dtos.output.charge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.com.nimblebaas.payment_gateway.entities.charge.ChargePayment;
import br.com.nimblebaas.payment_gateway.enums.charge.PaymentMethod;

class ChargePaymentOutputDTOTest {

    @Test
    void shouldCreateDTOFromChargePaymentWithCardNumber() {
        LocalDateTime paidAt = LocalDateTime.now();
        LocalDateTime cancelledAt = LocalDateTime.now().plusDays(1);
        
        ChargePayment chargePayment = ChargePayment.builder()
            .authorizationIdentifier("AUTH-123456")
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .cardNumber("1234567890123456")
            .paidAt(paidAt)
            .cancelledAt(cancelledAt)
            .build();

        ChargePaymentOutputDTO dto = new ChargePaymentOutputDTO(chargePayment);

        assertNotNull(dto);
        assertEquals("AUTH-123456", dto.getAuthorizationIdentifier());
        assertEquals(PaymentMethod.CREDIT_CARD, dto.getPaymentMethod());
        assertEquals("3456", dto.getLastCardDigits());
        assertEquals(paidAt, dto.getPaidAt());
        assertEquals(cancelledAt, dto.getCancelledAt());
    }

    @Test
    void shouldCreateDTOFromChargePaymentWithoutCardNumber() {
        LocalDateTime paidAt = LocalDateTime.now();
        
        ChargePayment chargePayment = ChargePayment.builder()
            .authorizationIdentifier("AUTH-789012")
            .paymentMethod(PaymentMethod.ACCOUNT_BALANCE)
            .cardNumber(null)
            .paidAt(paidAt)
            .cancelledAt(null)
            .build();

        ChargePaymentOutputDTO dto = new ChargePaymentOutputDTO(chargePayment);

        assertNotNull(dto);
        assertEquals("AUTH-789012", dto.getAuthorizationIdentifier());
        assertEquals(PaymentMethod.ACCOUNT_BALANCE, dto.getPaymentMethod());
        assertNull(dto.getLastCardDigits());
        assertEquals(paidAt, dto.getPaidAt());
        assertNull(dto.getCancelledAt());
    }

    @Test
    void shouldCreateDTOFromChargePaymentWithEmptyCardNumber() {
        LocalDateTime paidAt = LocalDateTime.now();
        
        ChargePayment chargePayment = ChargePayment.builder()
            .authorizationIdentifier("AUTH-345678")
            .paymentMethod(PaymentMethod.ACCOUNT_BALANCE)
            .cardNumber("")
            .paidAt(paidAt)
            .build();

        ChargePaymentOutputDTO dto = new ChargePaymentOutputDTO(chargePayment);

        assertNotNull(dto);
        assertEquals("AUTH-345678", dto.getAuthorizationIdentifier());
        assertEquals(PaymentMethod.ACCOUNT_BALANCE, dto.getPaymentMethod());
        assertNull(dto.getLastCardDigits());
        assertEquals(paidAt, dto.getPaidAt());
    }

    @Test
    void shouldCreateDTOFromChargePaymentWithBlankCardNumber() {
        LocalDateTime paidAt = LocalDateTime.now();
        
        ChargePayment chargePayment = ChargePayment.builder()
            .authorizationIdentifier("AUTH-456789")
            .paymentMethod(PaymentMethod.ACCOUNT_BALANCE)
            .cardNumber("   ")
            .paidAt(paidAt)
            .build();

        ChargePaymentOutputDTO dto = new ChargePaymentOutputDTO(chargePayment);

        assertNotNull(dto);
        assertEquals("AUTH-456789", dto.getAuthorizationIdentifier());
        assertEquals(PaymentMethod.ACCOUNT_BALANCE, dto.getPaymentMethod());
        assertNull(dto.getLastCardDigits());
        assertEquals(paidAt, dto.getPaidAt());
    }

    @Test
    void shouldExtractLastFourDigitsFromCardNumber() {
        ChargePayment chargePayment = ChargePayment.builder()
            .authorizationIdentifier("AUTH-111222")
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .cardNumber("4111111111111111")
            .paidAt(LocalDateTime.now())
            .build();

        ChargePaymentOutputDTO dto = new ChargePaymentOutputDTO(chargePayment);

        assertNotNull(dto);
        assertEquals("1111", dto.getLastCardDigits());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        ChargePaymentOutputDTO dto = new ChargePaymentOutputDTO();

        assertNotNull(dto);
        assertNull(dto.getAuthorizationIdentifier());
        assertNull(dto.getPaymentMethod());
        assertNull(dto.getLastCardDigits());
        assertNull(dto.getPaidAt());
        assertNull(dto.getCancelledAt());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        LocalDateTime paidAt = LocalDateTime.now();
        LocalDateTime cancelledAt = LocalDateTime.now().plusDays(1);

        ChargePaymentOutputDTO dto = new ChargePaymentOutputDTO(
            "AUTH-999888",
            PaymentMethod.CREDIT_CARD,
            "9999",
            paidAt,
            cancelledAt
        );

        assertNotNull(dto);
        assertEquals("AUTH-999888", dto.getAuthorizationIdentifier());
        assertEquals(PaymentMethod.CREDIT_CARD, dto.getPaymentMethod());
        assertEquals("9999", dto.getLastCardDigits());
        assertEquals(paidAt, dto.getPaidAt());
        assertEquals(cancelledAt, dto.getCancelledAt());
    }

    @Test
    void shouldSetAndGetAuthorizationIdentifier() {
        ChargePaymentOutputDTO dto = new ChargePaymentOutputDTO();
        dto.setAuthorizationIdentifier("AUTH-TEST");

        assertEquals("AUTH-TEST", dto.getAuthorizationIdentifier());
    }

    @Test
    void shouldSetAndGetPaymentMethod() {
        ChargePaymentOutputDTO dto = new ChargePaymentOutputDTO();
        dto.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        assertEquals(PaymentMethod.CREDIT_CARD, dto.getPaymentMethod());
    }

    @Test
    void shouldSetAndGetLastCardDigits() {
        ChargePaymentOutputDTO dto = new ChargePaymentOutputDTO();
        dto.setLastCardDigits("5678");

        assertEquals("5678", dto.getLastCardDigits());
    }

    @Test
    void shouldSetAndGetPaidAt() {
        ChargePaymentOutputDTO dto = new ChargePaymentOutputDTO();
        LocalDateTime paidAt = LocalDateTime.now();
        dto.setPaidAt(paidAt);

        assertEquals(paidAt, dto.getPaidAt());
    }

    @Test
    void shouldSetAndGetCancelledAt() {
        ChargePaymentOutputDTO dto = new ChargePaymentOutputDTO();
        LocalDateTime cancelledAt = LocalDateTime.now();
        dto.setCancelledAt(cancelledAt);

        assertEquals(cancelledAt, dto.getCancelledAt());
    }
}

