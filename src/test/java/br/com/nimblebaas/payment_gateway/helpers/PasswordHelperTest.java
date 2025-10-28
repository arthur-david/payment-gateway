package br.com.nimblebaas.payment_gateway.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.nimblebaas.payment_gateway.dtos.internal.validation.PasswordValidationResultRecord;

class PasswordHelperTest {

    private PasswordHelper passwordHelper;

    @BeforeEach
    void setUp() {
        passwordHelper = new PasswordHelper();
        ReflectionTestUtils.setField(passwordHelper, "passwordLengthMin", 8);
    }

    @Test
    void shouldReturnTrueForStrongPassword() {
        String password = "Senha@123";

        PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);

        assertNotNull(result);
        assertTrue(result.isStrong());
        assertEquals("A senha é forte", result.message());
    }

    @Test
    void shouldReturnFalseWhenPasswordIsTooShort() {
        String password = "Ab1@";

        PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);

        assertNotNull(result);
        assertFalse(result.isStrong());
        assertEquals("A senha deve ter pelo menos 8 caracteres", result.message());
    }

    @Test
    void shouldReturnFalseWhenPasswordHasNoUpperCase() {
        String password = "senha@123";

        PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);

        assertNotNull(result);
        assertFalse(result.isStrong());
        assertEquals("A senha deve conter pelo menos uma letra maiúscula", result.message());
    }

    @Test
    void shouldReturnFalseWhenPasswordHasNoLowerCase() {
        String password = "SENHA@123";

        PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);

        assertNotNull(result);
        assertFalse(result.isStrong());
        assertEquals("A senha deve conter pelo menos uma letra minúscula", result.message());
    }

    @Test
    void shouldReturnFalseWhenPasswordHasNoDigit() {
        String password = "Senha@abc";

        PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);

        assertNotNull(result);
        assertFalse(result.isStrong());
        assertEquals("A senha deve conter pelo menos um número", result.message());
    }

    @Test
    void shouldReturnFalseWhenPasswordHasNoSpecialCharacter() {
        String password = "Senha123";

        PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);

        assertNotNull(result);
        assertFalse(result.isStrong());
        assertEquals("A senha deve conter pelo menos um caractere especial '!@#$%^&*()'", result.message());
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

    @Test
    void shouldValidatePasswordWithExactlyMinimumRequirements() {
        String password = "Passw0rd!";

        PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);

        assertNotNull(result);
        assertTrue(result.isStrong());
        assertEquals("A senha é forte", result.message());
    }

    @Test
    void shouldValidateVeryLongStrongPassword() {
        String password = "SuperS3cur3P@ssw0rdWithL0tsOfCh@r@ct3rs!";

        PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);

        assertNotNull(result);
        assertTrue(result.isStrong());
        assertEquals("A senha é forte", result.message());
    }

    @Test
    void shouldValidatePasswordWithMultipleSpecialCharacters() {
        String password = "P@ssw0rd!#$";

        PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);

        assertNotNull(result);
        assertTrue(result.isStrong());
        assertEquals("A senha é forte", result.message());
    }

    @Test
    void shouldReturnFalseForEmptyPassword() {
        String password = "";

        PasswordValidationResultRecord result = passwordHelper.verifyIfIsStrong(password);

        assertNotNull(result);
        assertFalse(result.isStrong());
        assertEquals("A senha deve ter pelo menos 8 caracteres", result.message());
    }
}

