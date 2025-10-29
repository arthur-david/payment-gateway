package br.com.nimblebaas.payment_gateway.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StringHelperTest {

    @ParameterizedTest
    @CsvSource({
        "123.456.789-00,12345678900",
        "12345678900,12345678900",
        "abc123def456!@#,123456",
        "abcdef!@#$%,''",
        "'',''",
        "'123 456 789 00',12345678900"
    })
    void shouldExtractOnlyNumbers(String input, String expected) {
        String result = StringHelper.onlyNumbers(input);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
        "1234567890,7890",
        "4111111111111111,1111",
        "1234,1234",
        "abcdef1234,1234",
        "12345678900,8900",
        "0123456789012345678901234567890,7890"
    })
    void shouldGetLastFourDigits(String input, String expected) {
        String result = StringHelper.lastFourDigits(input);
        assertEquals(expected, result);
    }

    @Test
    void shouldThrowExceptionWhenStringIsShorterThanFour() {
        String input = "123";

        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            StringHelper.lastFourDigits(input);
        });
    }
}

