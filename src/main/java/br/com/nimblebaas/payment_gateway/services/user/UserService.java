package br.com.nimblebaas.payment_gateway.services.user;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.configs.authentication.UserAuthenticated;
import br.com.nimblebaas.payment_gateway.dtos.input.user.ChangePasswordInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.input.user.UserInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.user.UserOutputRecord;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.helpers.CPFHelper;
import br.com.nimblebaas.payment_gateway.helpers.StringHelper;
import br.com.nimblebaas.payment_gateway.repositories.user.UserRepository;
import br.com.nimblebaas.payment_gateway.services.account.AccountService;
import br.com.nimblebaas.payment_gateway.services.authentication.RefreshTokenService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserCreationValidator userCreationValidator;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    
    public UserOutputRecord create(UserInputRecord userInputRecord) {
        userCreationValidator.validate(userInputRecord);

        var encryptedPassword = passwordEncoder.encode(userInputRecord.password());

        var user = new User(userInputRecord, encryptedPassword);
        userRepository.save(user);

        accountService.openAccount(user);

        return new UserOutputRecord(user.getName(), user.getCpf(), user.getEmail());
    }

    public void changePassword(UserAuthenticated userAuthenticated, ChangePasswordInputRecord changePasswordInputRecord) {
        var user = getUserByCpf(userAuthenticated.getUsername());

        if (!passwordEncoder.matches(changePasswordInputRecord.currentPassword(), user.getPassword()))
            throw new BusinessRuleException(getClass(), BusinessRules.INVALID_PASSWORD, "Senha atual incorreta");

        userCreationValidator.validateIfPasswordIsStrong(changePasswordInputRecord.newPassword());

        user.setPassword(passwordEncoder.encode(changePasswordInputRecord.newPassword()));
        user.setLastChangedPasswordAt(LocalDateTime.now());

        refreshTokenService.revokeUserRefreshTokens(user);

        userRepository.save(user);
    }

    public UserOutputRecord getUser(String cpfOrEmail) {
        var user = findByCpfOrEmail(cpfOrEmail)
            .orElseThrow(() -> new BusinessRuleException(getClass(), BusinessRules.USER_NOT_FOUND, "Usuário não encontrado"));
            
        return new UserOutputRecord(user.getName(), user.getCpf(), user.getEmail());
    }

    public Optional<User> findByCpfOrEmail(String cpfOrEmail) {
        String cpf = null;
        String email = null;

        if (CPFHelper.isCPF(cpfOrEmail))
            cpf = StringHelper.onlyNumbers(cpfOrEmail.trim());
        else
            email = cpfOrEmail.trim().toLowerCase();

        return userRepository.findByCpfOrEmail(cpf, email);
    }

    public User getUserByCpf(String cpf) {
        return findByCpf(cpf)
            .orElseThrow(() -> new BusinessRuleException(getClass(), BusinessRules.USER_NOT_FOUND, "Usuário não encontrado com o CPF: %s", cpf));
    }

    public Optional<User> findByCpf(String cpf) {
        return userRepository.findByCpf(cpf);
    }
}
