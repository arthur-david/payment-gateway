package br.com.nimblebaas.payment_gateway.dtos.input.authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para autenticação no sistema")
public record LoginRequestRecord(
    @Schema(description = "CPF ou e-mail do usuário", example = "joao.silva@email.com")
    @NotBlank(message = "CPF ou e-mail é obrigatório")
    String cpfOrEmail,
    
    @Schema(description = "Senha do usuário", example = "SenhaSegura123!")
    @NotBlank(message = "Senha é obrigatória")
    String password
) {}

