package br.com.nimblebaas.payment_gateway.dtos.input.charge;

import br.com.nimblebaas.payment_gateway.enums.charge.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para pagamento de cobrança")
public record ChargePaymentInputRecord(

    @Schema(description = "Identificador único da cobrança", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "O identificador da cobrança é obrigatório")
    String identifier,

    @Schema(description = "Método de pagamento (BALANCE ou CREDIT_CARD)", example = "CREDIT_CARD")
    @NotNull(message = "O método de pagamento é obrigatório")
    PaymentMethod paymentMethod,

    @Schema(description = "Número do cartão de crédito (obrigatório se método = CREDIT_CARD)", example = "4111111111111111")
    String cardNumber,
    
    @Schema(description = "Data de expiração do cartão no formato MM/YYYY (obrigatório se método = CREDIT_CARD)", example = "12/2025")
    String cardExpirationDate,
    
    @Schema(description = "CVV do cartão (obrigatório se método = CREDIT_CARD)", example = "123")
    String cardCvv,
    
    @Schema(description = "Número de parcelas (obrigatório se método = CREDIT_CARD)", example = "1")
    Integer installments
) {
    
}
