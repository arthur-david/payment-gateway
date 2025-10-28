package br.com.nimblebaas.payment_gateway.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.nimblebaas.payment_gateway.dtos.internal.validation.CPFValidationResultRecord;

class CPFHelperTest {

    @Test
    void shouldReturnFalseWhenCPFIsNull() {
        CPFValidationResultRecord result = CPFHelper.validate(null);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals("CPF is required", result.message());
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678909", "123.456.789-09", "52998224725", "529.982.247-25"})
    void shouldReturnTrueForValidCPF(String cpf) {
        CPFValidationResultRecord result = CPFHelper.validate(cpf);

        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals("CPF is valid", result.message());
    }

    @ParameterizedTest
    @CsvSource({"123456789,'CPF must be 11 digits'", "123456789012,'CPF must be 11 digits'", "'','CPF must be 11 digits'"})
    void shouldReturnFalseForInvalidCPFLength(String cpf, String expectedMessage) {
        CPFValidationResultRecord result = CPFHelper.validate(cpf);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals(expectedMessage, result.message());
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

    @ParameterizedTest
    @ValueSource(strings = {"12345678909", "123.456.789-09"})
    void shouldReturnTrueWhenTextIsCPFFormat(String text) {
        assertTrue(CPFHelper.isCPF(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "test@email.com", "", "1234567890a", "123456789012", "123-456-789.09", "123.456.78909"})
    void shouldReturnFalseForInvalidCPFFormats(String text) {
        assertFalse(CPFHelper.isCPF(text));
    }

    @Test
    void shouldReturnFalseWhenTextIsNull() {
        assertFalse(CPFHelper.isCPF(null));
    }
}

