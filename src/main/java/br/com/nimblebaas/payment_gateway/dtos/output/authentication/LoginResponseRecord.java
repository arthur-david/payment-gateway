package br.com.nimblebaas.payment_gateway.dtos.output.authentication;

public record LoginResponseRecord(
    String accessToken,
    String refreshToken,
    Long expiresIn
) {}

