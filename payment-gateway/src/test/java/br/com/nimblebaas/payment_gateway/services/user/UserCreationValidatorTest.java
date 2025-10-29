package br.com.nimblebaas.payment_gateway.services.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.nimblebaas.payment_gateway.dtos.input.user.UserInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.internal.validation.PasswordValidationResultRecord;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.helpers.PasswordHelper;
import br.com.nimblebaas.payment_gateway.repositories.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserCreationValidatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHelper passwordHelper;

    @InjectMocks
    private UserCreationValidator userCreationValidator;

    private UserInputRecord validUserInput;

    @BeforeEach
    void setUp() {
        validUserInput = new UserInputRecord(
            "John Doe",
            "52998224725",
            "john@example.com",
            "StrongPass123!"
        );
    }

    @Test
    void validate_WithValidData_ShouldNotThrowException() {
        when(userRepository.existsByCpfOrEmail(anyString(), anyString())).thenReturn(false);
        when(passwordHelper.verifyIfIsStrong(anyString()))
            .thenReturn(new PasswordValidationResultRecord(true, "A senha é forte"));

        assertDoesNotThrow(() -> userCreationValidator.validate(validUserInput));
    }

    @Test
    void validate_WithInvalidCpf_ShouldThrowException() {
        UserInputRecord invalidCpfInput = new UserInputRecord(
            "John Doe",
            "11111111111",
            "john@example.com",
            "StrongPass123!"
        );

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> userCreationValidator.validate(invalidCpfInput)
        );

        assertEquals(BusinessRules.INVALID_INPUT_DATA.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void validate_WithExistingUser_ShouldThrowException() {
        when(userRepository.existsByCpfOrEmail(anyString(), anyString())).thenReturn(true);

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> userCreationValidator.validate(validUserInput)
        );

        assertEquals(BusinessRules.USER_ALREADY_EXISTS.name(), exception.getErrorDTO().getReason());
        assertEquals("O usuário informado já está cadastrado", exception.getErrorDTO().getDetails());
    }

    @Test
    void validate_WithWeakPassword_ShouldThrowException() {
        when(userRepository.existsByCpfOrEmail(anyString(), anyString())).thenReturn(false);
        when(passwordHelper.verifyIfIsStrong(anyString()))
            .thenReturn(new PasswordValidationResultRecord(false, "A senha deve conter pelo menos uma letra maiúscula"));

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> userCreationValidator.validate(validUserInput)
        );

        assertEquals(BusinessRules.INVALID_INPUT_DATA.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void validateIfPasswordIsStrong_WithStrongPassword_ShouldNotThrowException() {
        when(passwordHelper.verifyIfIsStrong(anyString()))
            .thenReturn(new PasswordValidationResultRecord(true, "A senha é forte"));

        assertDoesNotThrow(() -> userCreationValidator.validateIfPasswordIsStrong("StrongPass123!"));
    }

    @Test
    void validateIfPasswordIsStrong_WithWeakPassword_ShouldThrowException() {
        when(passwordHelper.verifyIfIsStrong(anyString()))
            .thenReturn(new PasswordValidationResultRecord(false, "A senha deve conter pelo menos um número"));

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> userCreationValidator.validateIfPasswordIsStrong("weakpass")
        );

        assertEquals(BusinessRules.INVALID_INPUT_DATA.name(), exception.getErrorDTO().getReason());
    }
}

