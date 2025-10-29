package br.com.nimblebaas.payment_gateway.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;

class BusinessRuleExceptionTest {

    @Test
    void shouldCreateBusinessRuleExceptionWithDefaultBadRequest() {
        String details = "Dados inválidos";
        
        BusinessRuleException exception = new BusinessRuleException(
            getClass(), 
            BusinessRules.INVALID_INPUT_DATA, 
            details
        );

        assertNotNull(exception);
        assertEquals(getClass(), exception.getSource());
        assertNotNull(exception.getErrorDTO());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getErrorDTO().getStatus());
        assertEquals(BusinessRules.INVALID_INPUT_DATA.name(), exception.getErrorDTO().getReason());
        assertEquals(details, exception.getErrorDTO().getDetails());
    }

    @Test
    void shouldCreateBusinessRuleExceptionWithCustomStatus() {
        String details = "Usuário não encontrado";
        
        BusinessRuleException exception = new BusinessRuleException(
            HttpStatus.NOT_FOUND,
            getClass(), 
            BusinessRules.USER_NOT_FOUND, 
            details
        );

        assertNotNull(exception);
        assertEquals(getClass(), exception.getSource());
        assertNotNull(exception.getErrorDTO());
        assertEquals(HttpStatus.NOT_FOUND, exception.getErrorDTO().getStatus());
        assertEquals(BusinessRules.USER_NOT_FOUND.name(), exception.getErrorDTO().getReason());
        assertEquals(details, exception.getErrorDTO().getDetails());
    }

    @Test
    void shouldCreateBusinessRuleExceptionWithFormattedDetails() {
        String cpf = "12345678900";
        
        BusinessRuleException exception = new BusinessRuleException(
            getClass(), 
            BusinessRules.USER_NOT_FOUND, 
            "Usuário não encontrado com CPF: %s",
            cpf
        );

        assertNotNull(exception);
        assertEquals(getClass(), exception.getSource());
        assertNotNull(exception.getErrorDTO());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getErrorDTO().getStatus());
        assertEquals(BusinessRules.USER_NOT_FOUND.name(), exception.getErrorDTO().getReason());
        assertEquals("Usuário não encontrado com CPF: 12345678900", exception.getErrorDTO().getDetails());
    }

    @Test
    void shouldCreateBusinessRuleExceptionWithCustomStatusAndFormattedDetails() {
        String email = "teste@email.com";
        
        BusinessRuleException exception = new BusinessRuleException(
            HttpStatus.UNAUTHORIZED,
            getClass(), 
            BusinessRules.INVALID_CREDENTIALS, 
            "Credenciais inválidas para: %s",
            email
        );

        assertNotNull(exception);
        assertEquals(getClass(), exception.getSource());
        assertNotNull(exception.getErrorDTO());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getErrorDTO().getStatus());
        assertEquals(BusinessRules.INVALID_CREDENTIALS.name(), exception.getErrorDTO().getReason());
        assertEquals("Credenciais inválidas para: teste@email.com", exception.getErrorDTO().getDetails());
    }

    @Test
    void shouldGetMessageFromErrorDTO() {
        String details = "Token inválido";
        
        BusinessRuleException exception = new BusinessRuleException(
            getClass(), 
            BusinessRules.INVALID_TOKEN, 
            details
        );

        String expectedMessage = BusinessRules.INVALID_TOKEN.name() + ": " + details;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldReturnMessageFromErrorDTO() {
        BusinessRuleException exception = new BusinessRuleException(
            getClass(), 
            BusinessRules.INVALID_TOKEN, 
            "teste"
        );
        
        assertNotNull(exception.getMessage());
        assertNotNull(exception.getErrorDTO());
    }
}

