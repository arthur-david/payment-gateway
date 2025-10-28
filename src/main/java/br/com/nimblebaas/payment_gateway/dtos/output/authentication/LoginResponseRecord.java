package br.com.nimblebaas.payment_gateway.dtos.output.authentication;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação com tokens de acesso")
public record LoginResponseRecord(
    @Schema(description = "Token JWT para autenticação nas requisições", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,
    
    @Schema(description = "Token para renovação do access token", example = "550e8400-e29b-41d4-a716-446655440000")
    String refreshToken,
    
    @Schema(description = "Tempo de expiração do access token em segundos", example = "3600")
    Long expiresIn
) {}

