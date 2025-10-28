package br.com.nimblebaas.payment_gateway.dtos.output.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados públicos do usuário")
public record UserOutputRecord(
    @Schema(description = "Nome completo do usuário", example = "João da Silva")
    String name,
    
    @Schema(description = "CPF do usuário", example = "12345678900")
    String cpf,
    
    @Schema(description = "E-mail do usuário", example = "joao.silva@email.com")
    String email
) {}
