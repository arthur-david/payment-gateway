package br.com.nimblebaas.payment_gateway.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.nimblebaas.payment_gateway.dtos.internal.validation.PasswordValidationResultRecord;

class PasswordHelperTest {

    private PasswordHelper passwordHelper;

    @BeforeEach
    void setUp() {
        passwordHelper = new PasswordHelper();
        ReflectionTestUtils.setField(passwordHelper, "passwordLengthMin", 8);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Senha@123", "Passw0rd!", "SuperS3cur3P@ssw0rdWithL0tsOfCh@r@ct3rs!", "P@ssw0rd!#$"})
    void shouldReturnTrueForStrongPassword(String password) {
        PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);

        assertNotNull(result);
        assertTrue(result.isStrong());
        assertEquals("A senha é forte", result.message());
    }

    @ParameterizedTest
    @CsvSource({
        "Ab1@,'A senha deve ter pelo menos 8 caracteres'",
        "senha@123,'A senha deve conter pelo menos uma letra maiúscula'",
        "SENHA@123,'A senha deve conter pelo menos uma letra minúscula'",
        "Senha@abc,'A senha deve conter pelo menos um número'",
        "Senha123,'A senha deve conter pelo menos um caractere especial ''!@#$%^&*()'''",
        "'','A senha deve ter pelo menos 8 caracteres'"
    })
    void shouldReturnFalseForWeakPasswords(String password, String expectedMessage) {
        PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);

        assertNotNull(result);
        assertFalse(result.isStrong());
        assertEquals(expectedMessage, result.message());
    }

    @Test
    void shouldAcceptAllSpecialCharacters() {
        String[] passwords = {
            "Senha!123", "Senha@123", "Senha#123", "Senha$123", "Senha%123",
            "Senha^123", "Senha&123", "Senha*123", "Senha(123", "Senha)123"
        };

        for (String password : passwords) {
            PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);
            assertTrue(result.isStrong(), "Senha com " + password + " deveria ser válida");
        }
    }

    @Test
    void shouldReturnFalseForPasswordWithOnlyMinimumLength() {
        String password = "12345678";

        PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);

        assertNotNull(result);
        assertFalse(result.isStrong());
    }


}

