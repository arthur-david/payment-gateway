package br.com.nimblebaas.payment_gateway.dtos.input.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para alteração de senha")
public record ChangePasswordInputRecord(
    @Schema(description = "Senha atual do usuário", example = "SenhaAtual123!")
    @NotBlank(message = "A senha atual é obrigatória")
    String currentPassword,
    
    @Schema(description = "Nova senha do usuário (mínimo 8 caracteres)", example = "NovaSenha456!")
    @NotBlank(message = "A nova senha é obrigatória")
    String newPassword
) {}
