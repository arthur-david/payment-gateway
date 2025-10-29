package br.com.nimblebaas.payment_gateway.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.com.nimblebaas.payment_gateway.dtos.internal.validation.PasswordValidationResultRecord;

@Component
public class PasswordHelper {

    @Value("${app.password.length.min}")
    private int passwordLengthMin;
    
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()";

    public PasswordValidationResultRecord verifyIfIsStrong(String password) {
        if (password.length() < passwordLengthMin)
            return new PasswordValidationResultRecord(false, 
                String.format("A senha deve ter pelo menos %d caracteres", passwordLengthMin));
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            }
            if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (SPECIAL_CHARACTERS.indexOf(c) >= 0) {
                hasSpecialChar = true;
            }
        }
        
        if (!hasUpperCase)
            return new PasswordValidationResultRecord(false, 
                "A senha deve conter pelo menos uma letra maiúscula");
        
        if (!hasLowerCase)
            return new PasswordValidationResultRecord(false, 
                "A senha deve conter pelo menos uma letra minúscula");
        
        if (!hasDigit)
            return new PasswordValidationResultRecord(false, 
                "A senha deve conter pelo menos um número");
        
        if (!hasSpecialChar)
            return new PasswordValidationResultRecord(false, 
                "A senha deve conter pelo menos um caractere especial '!@#$%^&*()'");
        
        return new PasswordValidationResultRecord(true, "A senha é forte");
    }
}
