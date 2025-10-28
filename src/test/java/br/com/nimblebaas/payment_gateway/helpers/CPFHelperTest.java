package br.com.nimblebaas.payment_gateway.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.com.nimblebaas.payment_gateway.dtos.internal.validation.CPFValidationResultRecord;

class CPFHelperTest {

    @Test
    void shouldReturnFalseWhenCPFIsNull() {
        CPFValidationResultRecord result = CPFHelper.validate(null);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals("CPF is required", result.message());
    }

    @Test
    void shouldReturnTrueForValidCPF() {
        String cpf = "12345678909";

        CPFValidationResultRecord result = CPFHelper.validate(cpf);

        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals("CPF is valid", result.message());
    }

    @Test
    void shouldReturnTrueForValidCPFWithFormatting() {
        String cpf = "123.456.789-09";

        CPFValidationResultRecord result = CPFHelper.validate(cpf);

        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals("CPF is valid", result.message());
    }

    @Test
    void shouldReturnFalseForCPFWithLessThan11Digits() {
        String cpf = "123456789";

        CPFValidationResultRecord result = CPFHelper.validate(cpf);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals("CPF must be 11 digits", result.message());
    }

    @Test
    void shouldReturnFalseForCPFWithMoreThan11Digits() {
        String cpf = "123456789012";

        CPFValidationResultRecord result = CPFHelper.validate(cpf);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals("CPF must be 11 digits", result.message());
    }

    @Test
    void shouldReturnFalseForCPFWithAllSameDigits() {
        String[] invalidCpfs = {
            "00000000000", "11111111111", "22222222222", "33333333333",
            "44444444444", "55555555555", "66666666666", "77777777777",
            "88888888888", "99999999999"
        };

        for (String cpf : invalidCpfs) {
            CPFValidationResultRecord result = CPFHelper.validate(cpf);
            assertFalse(result.isValid(), "CPF " + cpf + " deveria ser inválido");
            assertEquals("CPF must be 11 digits", result.message());
        }
    }

    @Test
    void shouldReturnFalseForInvalidCPFWithWrongVerificationDigits() {
        String cpf = "12345678900";

        CPFValidationResultRecord result = CPFHelper.validate(cpf);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals("CPF is invalid", result.message());
    }

    @Test
    void shouldValidateMultipleValidCPFs() {
        String[] validCpfs = {
            "12345678909",
            "11144477735",
            "52998224725"
        };

        for (String cpf : validCpfs) {
            CPFValidationResultRecord result = CPFHelper.validate(cpf);
            assertTrue(result.isValid(), "CPF " + cpf + " deveria ser válido");
            assertEquals("CPF is valid", result.message());
        }
    }

    @Test
    void shouldReturnTrueWhenTextIsCPFFormat() {
        assertTrue(CPFHelper.isCPF("12345678909"));
    }

    @Test
    void shouldReturnTrueWhenTextIsCPFFormatted() {
        assertTrue(CPFHelper.isCPF("123.456.789-09"));
    }

    @Test
    void shouldReturnFalseWhenTextIsNotCPF() {
        assertFalse(CPFHelper.isCPF("123456789"));
    }

    @Test
    void shouldReturnFalseWhenTextIsEmail() {
        assertFalse(CPFHelper.isCPF("test@email.com"));
    }

    @Test
    void shouldReturnFalseWhenTextIsNull() {
        assertFalse(CPFHelper.isCPF(null));
    }

    @Test
    void shouldReturnFalseWhenTextIsEmpty() {
        assertFalse(CPFHelper.isCPF(""));
    }

    @Test
    void shouldReturnFalseWhenTextHasLetters() {
        assertFalse(CPFHelper.isCPF("1234567890a"));
    }

    @Test
    void shouldReturnFalseWhenTextIsTooLong() {
        assertFalse(CPFHelper.isCPF("123456789012"));
    }

    @Test
    void shouldReturnFalseWhenTextHasIncorrectFormatting() {
        assertFalse(CPFHelper.isCPF("123-456-789.09"));
    }

    @Test
    void shouldReturnFalseForPartiallyFormattedCPF() {
        assertFalse(CPFHelper.isCPF("123.456.78909"));
    }

    @Test
    void shouldValidateCPFWithOnlyNumbers() {
        String cpf = "52998224725";

        CPFValidationResultRecord result = CPFHelper.validate(cpf);

        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals("CPF is valid", result.message());
    }

    @Test
    void shouldReturnFalseForEmptyCPF() {
        String cpf = "";

        CPFValidationResultRecord result = CPFHelper.validate(cpf);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals("CPF must be 11 digits", result.message());
    }

    @Test
    void shouldRemoveFormattingBeforeValidation() {
        String cpf = "529.982.247-25";

        CPFValidationResultRecord result = CPFHelper.validate(cpf);

        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals("CPF is valid", result.message());
    }
}

