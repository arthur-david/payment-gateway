package br.com.nimblebaas.payment_gateway.dtos.input.authentication;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestRecord(
    @NotBlank(message = "Refresh token é obrigatório")
    String refreshToken
) {}

