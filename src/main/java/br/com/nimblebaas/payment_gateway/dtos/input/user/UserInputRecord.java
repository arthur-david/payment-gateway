package br.com.nimblebaas.payment_gateway.dtos.input.user;

import static java.util.Objects.isNull;

import br.com.nimblebaas.payment_gateway.helpers.StringHelper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserInputRecord(

    @NotBlank(message = "Nome é obrigatório")
    String name,

    @NotBlank(message = "CPF é obrigatório")
    String cpf,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String email,
    
    @NotBlank(message = "Senha é obrigatória")
    String password
) {

    public String getCpfOnlyNumbers() {
        return isNull(cpf) ? null : StringHelper.onlyNumbers(cpf);
    }
}
