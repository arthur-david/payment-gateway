package br.com.nimblebaas.payment_gateway.dtos.internal.authorizer;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
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
public class GetAuthorizerDTO {

    @NotBlank(message = "CPF não informado")
    private String cpf;

    @NotNull(message = "Valor do depósito não informado")
    @DecimalMin(value = "0.01", message = "Valor do depósito deve ser maior que 0")
    private BigDecimal amount;

    @NotBlank(message = "Identificador do depósito não informado")
    private String identifier;

    private AuthorizerCardDetailsDTO cardDetails;
}
