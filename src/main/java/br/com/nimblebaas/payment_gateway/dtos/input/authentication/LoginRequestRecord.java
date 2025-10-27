package br.com.nimblebaas.payment_gateway.dtos.input.authentication;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestRecord(
    @NotBlank(message = "CPF ou e-mail é obrigatório")
    String cpfOrEmail,
    
    @NotBlank(message = "Senha é obrigatória")
    String password
) {}

