package br.com.nimblebaas.payment_gateway.dtos.input.charge;

import br.com.nimblebaas.payment_gateway.enums.charge.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChargePaymentInputRecord(

    @NotBlank(message = "O identificador da cobrança é obrigatório")
    String identifier,

    @NotNull(message = "O método de pagamento é obrigatório")
    PaymentMethod paymentMethod,

    String cardNumber,
    String cardExpirationDate,
    String cardCvv,
    Integer installments
) {
    
}
