package br.com.nimblebaas.payment_gateway.dtos.internal.charge;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargePaymentInputRecord;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargePaymentDTO {

    @NotNull(message = "A cobrança é obrigatória")
    @JsonIgnore
    private Charge charge;

    private String cardNumber;
    private String cardExpirationDate;
    private String cardCvv;
    private Integer installments;

    public ChargePaymentDTO(ChargePaymentInputRecord chargePaymentInputRecord, Charge charge) {
        setCharge(charge);
        setCardNumber(chargePaymentInputRecord.cardNumber());
        setCardExpirationDate(chargePaymentInputRecord.cardExpirationDate());
        setCardCvv(chargePaymentInputRecord.cardCvv());
        setInstallments(chargePaymentInputRecord.installments());
    }
}
