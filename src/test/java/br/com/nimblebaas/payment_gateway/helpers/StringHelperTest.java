package br.com.nimblebaas.payment_gateway.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StringHelperTest {

    @Test
    void shouldRemoveAllNonNumericCharacters() {
        String input = "123.456.789-00";

        String result = StringHelper.onlyNumbers(input);

        assertEquals("12345678900", result);
    }

    @Test
    void shouldReturnSameStringWhenOnlyNumbers() {
        String input = "12345678900";

        String result = StringHelper.onlyNumbers(input);

        assertEquals("12345678900", result);
    }

    @Test
    void shouldRemoveLettersAndSpecialCharacters() {
        String input = "abc123def456!@#";

        String result = StringHelper.onlyNumbers(input);

        assertEquals("123456", result);
    }

    @Test
    void shouldReturnEmptyStringWhenNoNumbers() {
        String input = "abcdef!@#$%";

        String result = StringHelper.onlyNumbers(input);

        assertEquals("", result);
    }

    @Test
    void shouldReturnEmptyStringWhenInputIsEmpty() {
        String input = "";

        String result = StringHelper.onlyNumbers(input);

        assertEquals("", result);
    }

    @Test
    void shouldRemoveSpacesAndOtherCharacters() {
        String input = "123 456 789 00";

        String result = StringHelper.onlyNumbers(input);

        assertEquals("12345678900", result);
    }

    @Test
    void shouldGetLastFourDigits() {
        String input = "1234567890";

        String result = StringHelper.lastFourDigits(input);

        assertEquals("7890", result);
    }

    @Test
    void shouldGetLastFourDigitsFromCreditCard() {
        String input = "4111111111111111";

        String result = StringHelper.lastFourDigits(input);

        assertEquals("1111", result);
    }

    @Test
    void shouldGetLastFourDigitsWhenExactlyFourDigits() {
        String input = "1234";

        String result = StringHelper.lastFourDigits(input);

        assertEquals("1234", result);
    }

    @Test
    void shouldThrowExceptionWhenStringIsShorterThanFour() {
        String input = "123";

        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            StringHelper.lastFourDigits(input);
        });
    }

    @Test
    void shouldGetLastFourCharactersIncludingNonNumeric() {
        String input = "abcdef1234";

        String result = StringHelper.lastFourDigits(input);

        assertEquals("1234", result);
    }

    @Test
    void shouldGetLastFourDigitsFromCPF() {
        String input = "12345678900";

        String result = StringHelper.lastFourDigits(input);

        assertEquals("8900", result);
    }

    @Test
    void shouldGetLastFourDigitsFromLongString() {
        String input = "0123456789012345678901234567890";

        String result = StringHelper.lastFourDigits(input);

        assertEquals("7890", result);
    }
}

