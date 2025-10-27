package br.com.nimblebaas.payment_gateway.services.user;

import org.springframework.stereotype.Component;

import br.com.nimblebaas.payment_gateway.dtos.input.user.UserInputRecord;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.helpers.CPFHelper;
import br.com.nimblebaas.payment_gateway.helpers.PasswordHelper;
import br.com.nimblebaas.payment_gateway.repositories.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserCreationValidator {

    private final UserRepository userRepository;
    private final PasswordHelper passwordHelper;

    public void validate(@Valid UserInputRecord userInputRecord) {
        var cpf = userInputRecord.getCpfOnlyNumbers();

        validateCpf(cpf);
        
        validateIfUserAlreadyExists(cpf, userInputRecord.email());

        validateIfPasswordIsStrong(userInputRecord.password());
    }

    private void validateCpf(String cpf) {
        var cpfValidationResult = CPFHelper.validate(cpf);
        if (!cpfValidationResult.isValid())
            throw new BusinessRuleException(getClass(), BusinessRules.INVALID_INPUT_DATA, cpfValidationResult.message());
    }

    private void validateIfUserAlreadyExists(String cpf, String email) {
        var exists = userRepository.existsByCpfOrEmail(cpf, email);
        if (exists)
            throw new BusinessRuleException(getClass(), BusinessRules.USER_ALREADY_EXISTS, "O usuário informado já está cadastrado");
    }

    public void validateIfPasswordIsStrong(String password) {
        var passwordValidationResult = passwordHelper.verifyIfIsStrong(password);
        if (!passwordValidationResult.isStrong())
            throw new BusinessRuleException(getClass(), BusinessRules.INVALID_INPUT_DATA, passwordValidationResult.message());
    }
}    