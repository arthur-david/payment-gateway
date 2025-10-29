package br.com.nimblebaas.payment_gateway.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
    }

    @Test
    void shouldHandleBusinessRuleException() {
        BusinessRuleException exception = new BusinessRuleException(
            getClass(),
            BusinessRules.INVALID_INPUT_DATA,
            "Dados inválidos"
        );

        ResponseEntity<ErrorDTO> response = handler.handleBusinessRuleException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getBody().getStatus());
        assertEquals(BusinessRules.INVALID_INPUT_DATA.name(), response.getBody().getReason());
        assertEquals("Dados inválidos", response.getBody().getDetails());
    }

    @Test
    void shouldHandleBusinessRuleExceptionWithCustomStatus() {
        BusinessRuleException exception = new BusinessRuleException(
            HttpStatus.UNAUTHORIZED,
            getClass(),
            BusinessRules.INVALID_TOKEN,
            "Token inválido"
        );

        ResponseEntity<ErrorDTO> response = handler.handleBusinessRuleException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getBody().getStatus());
        assertEquals(BusinessRules.INVALID_TOKEN.name(), response.getBody().getReason());
        assertEquals("Token inválido", response.getBody().getDetails());
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        BindingResult bindingResult = mock(BindingResult.class);

        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("user", "name", "não pode ser vazio"));
        fieldErrors.add(new FieldError("user", "cpf", "deve ser válido"));

        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorDTO> response = handler.handleValidationException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getBody().getStatus());
        assertEquals("VALIDATION_ERROR", response.getBody().getReason());
        assertTrue(response.getBody().getDetails().contains("Erros de validação:"));
        assertTrue(response.getBody().getDetails().contains("name: não pode ser vazio"));
        assertTrue(response.getBody().getDetails().contains("cpf: deve ser válido"));
    }

    @Test
    void shouldHandleMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("id");
        when(exception.getValue()).thenReturn("abc");
        when(exception.getRequiredType()).thenReturn((Class) Long.class);

        ResponseEntity<ErrorDTO> response = handler.handleTypeMismatchException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getBody().getStatus());
        assertEquals("TYPE_MISMATCH", response.getBody().getReason());
        assertTrue(response.getBody().getDetails().contains("id"));
        assertTrue(response.getBody().getDetails().contains("abc"));
        assertTrue(response.getBody().getDetails().contains("Long"));
    }

    @Test
    void shouldHandleMethodArgumentTypeMismatchExceptionWithNullRequiredType() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("id");
        when(exception.getValue()).thenReturn("abc");
        when(exception.getRequiredType()).thenReturn(null);

        ResponseEntity<ErrorDTO> response = handler.handleTypeMismatchException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getDetails().contains("desconhecido"));
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");

        ResponseEntity<ErrorDTO> response = handler.handleIllegalArgumentException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getBody().getStatus());
        assertEquals("ILLEGAL_ARGUMENT", response.getBody().getReason());
        assertEquals("Argumento inválido", response.getBody().getDetails());
    }

    @Test
    void shouldHandleNoResourceFoundException() {
        NoResourceFoundException exception = mock(NoResourceFoundException.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users");
        when(webRequest.getContextPath()).thenReturn("/app");

        ResponseEntity<ErrorDTO> response = handler.handleGenericException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getBody().getStatus());
        assertEquals("NOT_FOUND", response.getBody().getReason());
        assertTrue(response.getBody().getDetails().contains("não foi encontrado"));
    }

    @Test
    void shouldHandleGenericException() {
        Exception exception = new Exception("Erro genérico");

        ResponseEntity<ErrorDTO> response = handler.handleGenericException(exception, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getBody().getStatus());
        assertEquals("INTERNAL_ERROR", response.getBody().getReason());
        assertEquals("Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.", response.getBody().getDetails());
    }
}

