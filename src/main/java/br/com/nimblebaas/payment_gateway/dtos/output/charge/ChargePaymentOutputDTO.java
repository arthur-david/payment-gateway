package br.com.nimblebaas.payment_gateway.dtos.output.charge;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.LocalDateTime;

import br.com.nimblebaas.payment_gateway.entities.charge.ChargePayment;
import br.com.nimblebaas.payment_gateway.enums.charge.PaymentMethod;
import br.com.nimblebaas.payment_gateway.helpers.StringHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChargePaymentOutputDTO {

    private String authorizationIdentifier;
    private PaymentMethod paymentMethod;
    private String lastCardDigits;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;

    public ChargePaymentOutputDTO(ChargePayment chargePayment) {
        setAuthorizationIdentifier(chargePayment.getAuthorizationIdentifier());
        setPaymentMethod(chargePayment.getPaymentMethod());
        setPaidAt(chargePayment.getPaidAt());
        setCancelledAt(chargePayment.getCancelledAt());

        if (isNotBlank(chargePayment.getCardNumber()))
            setLastCardDigits(StringHelper.lastFourDigits(chargePayment.getCardNumber()));
    }
}
