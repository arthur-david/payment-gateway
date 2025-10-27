package br.com.nimblebaas.payment_gateway.dtos.input.charge;

import java.math.BigDecimal;

import br.com.nimblebaas.payment_gateway.helpers.StringHelper;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChargeInputRecord(

    @NotBlank(message = "O CPF de destino é obrigatório")
    String destinationCpf,

    @NotNull(message = "O valor da cobrança é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor da cobrança deve ser maior que 0")
    BigDecimal amount,

    String description
) {

    public String getDestinationCpfOnlyNumbers() {
        return StringHelper.onlyNumbers(destinationCpf);
    }
}
