package br.com.nimblebaas.payment_gateway.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.com.nimblebaas.payment_gateway.dtos.internal.validation.PasswordValidationResultRecord;

@Component
public class PasswordHelper {

    @Value("${app.password.length.min}")
    private int passwordLengthMin;

    public PasswordValidationResultRecord verifyIfIsStrong(String password) {
        if (password.length() < passwordLengthMin)
            return new PasswordValidationResultRecord(false, 
                String.format("A senha deve ter pelo menos %d caracteres", passwordLengthMin));
        
        if (!password.matches(".*[A-Z].*"))
            return new PasswordValidationResultRecord(false, 
                "A senha deve conter pelo menos uma letra maiúscula");
        
        if (!password.matches(".*[a-z].*"))
            return new PasswordValidationResultRecord(false, 
                "A senha deve conter pelo menos uma letra minúscula");
        
        if (!password.matches(".*\\d.*"))
            return new PasswordValidationResultRecord(false, 
                "A senha deve conter pelo menos um número");
        
        if (!password.matches(".*[!@#$%^&*()].*"))
            return new PasswordValidationResultRecord(false, 
                "A senha deve conter pelo menos um caractere especial '!@#$%^&*()'");
        
        return new PasswordValidationResultRecord(true, "A senha é forte");
    }
}
