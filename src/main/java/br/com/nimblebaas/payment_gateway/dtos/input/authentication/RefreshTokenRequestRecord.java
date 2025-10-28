package br.com.nimblebaas.payment_gateway.dtos.input.authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para renovação de token de acesso")
public record RefreshTokenRequestRecord(
    @Schema(description = "Refresh token obtido no login", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "Refresh token é obrigatório")
    String refreshToken
) {}

