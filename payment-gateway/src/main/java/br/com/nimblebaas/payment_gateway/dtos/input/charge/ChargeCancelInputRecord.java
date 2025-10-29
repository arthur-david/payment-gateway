package br.com.nimblebaas.payment_gateway.dtos.input.charge;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para cancelamento de cobrança")
public record ChargeCancelInputRecord(
    @Schema(description = "Identificador único da cobrança a ser cancelada", example = "550e8400-e29b-41d4-a716-446655440000")
    String identifier
) {}
