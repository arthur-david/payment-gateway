package br.com.nimblebaas.payment_gateway.helpers;

import static java.util.Objects.isNull;

import br.com.nimblebaas.payment_gateway.dtos.internal.validation.CPFValidationResultRecord;

public class CPFHelper {

    private CPFHelper() {}
    
    public static CPFValidationResultRecord validate(String cpf) {
        if (isNull(cpf))
            return new CPFValidationResultRecord(false, "CPF is required");

        cpf = StringHelper.onlyNumbers(cpf);

        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}"))
            return new CPFValidationResultRecord(false, "CPF must be 11 digits");

        int[] digits = cpf.chars().map(Character::getNumericValue).toArray();

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += digits[i] * (10 - i);
        }
        
        int remainder = sum % 11;
        int firstDigit = remainder < 2 ? 0 : 11 - remainder;

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += digits[i] * (11 - i);
        }
        
        remainder = sum % 11;
        int secondDigit = remainder < 2 ? 0 : 11 - remainder;

        if (digits[9] == firstDigit && digits[10] == secondDigit)
            return new CPFValidationResultRecord(true, "CPF is valid");
        
        return new CPFValidationResultRecord(false, "CPF is invalid");
    }

    public static boolean isCPF(String text) {
        if (isNull(text))
            return false;

        return text.matches("\\d{11}") || text.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");
    }
}
