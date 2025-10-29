package br.com.nimblebaas.payment_gateway.dtos.input.user;

import static java.util.Objects.isNull;

import br.com.nimblebaas.payment_gateway.helpers.StringHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para registro de novo usuário")
public record UserInputRecord(

    @Schema(description = "Nome completo do usuário", example = "João da Silva")
    @NotBlank(message = "Nome é obrigatório")
    String name,

    @Schema(description = "CPF do usuário (com ou sem formatação)", example = "123.456.789-00")
    @NotBlank(message = "CPF é obrigatório")
    String cpf,

    @Schema(description = "E-mail do usuário", example = "joao.silva@email.com")
    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String email,
    
    @Schema(description = "Senha do usuário (mínimo 8 caracteres)", example = "SenhaSegura123!")
    @NotBlank(message = "Senha é obrigatória")
    String password
) {

    public String getCpfOnlyNumbers() {
        return isNull(cpf) ? null : StringHelper.onlyNumbers(cpf);
    }
}
