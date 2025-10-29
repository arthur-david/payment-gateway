package br.com.nimblebaas.payment_gateway.dtos.internal.authorizer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class AuthorizerCardDetailsDTO {

    @NotBlank(message = "Número do cartão não informado")
    private String cardNumber;

    @NotBlank(message = "CVV do cartão não informado")
    private String cardCvv;

    @NotBlank(message = "Data de expiração do cartão não informada")
    private String cardExpirationDate;

    @NotNull(message = "Número de parcelas não informado")
    @Min(value = 1, message = "Número de parcelas deve ser maior que 0")
    @Max(value = 12, message = "Número de parcelas deve ser menor que 13")
    private Integer installments;
}
