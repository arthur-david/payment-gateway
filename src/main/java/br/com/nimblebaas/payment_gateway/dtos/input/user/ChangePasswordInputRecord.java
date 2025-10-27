package br.com.nimblebaas.payment_gateway.dtos.input.user;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordInputRecord(
    @NotBlank(message = "A senha atual é obrigatória")
    String currentPassword,
    @NotBlank(message = "A nova senha é obrigatória")
    String newPassword
) {}
