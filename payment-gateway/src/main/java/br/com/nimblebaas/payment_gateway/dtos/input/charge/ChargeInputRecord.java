package br.com.nimblebaas.payment_gateway.dtos.input.charge;

import java.math.BigDecimal;

import br.com.nimblebaas.payment_gateway.helpers.StringHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para criação de cobrança")
public record ChargeInputRecord(

    @Schema(description = "CPF do destinatário da cobrança (com ou sem formatação)", example = "123.456.789-00")
    @NotBlank(message = "O CPF de destino é obrigatório")
    String destinationCpf,

    @Schema(description = "Valor da cobrança em reais", example = "50.00")
    @NotNull(message = "O valor da cobrança é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor da cobrança deve ser maior que 0")
    BigDecimal amount,

    @Schema(description = "Descrição opcional da cobrança", example = "Pagamento de serviço prestado")
    String description
) {

    public String getDestinationCpfOnlyNumbers() {
        return StringHelper.onlyNumbers(destinationCpf);
    }
}
