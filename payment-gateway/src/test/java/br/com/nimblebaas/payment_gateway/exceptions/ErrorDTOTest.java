package br.com.nimblebaas.payment_gateway.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ErrorDTOTest {

    @Test
    void shouldCreateErrorDTOWithBuilder() {
        ErrorDTO errorDTO = ErrorDTO.builder()
            .status(HttpStatus.BAD_REQUEST)
            .reason("INVALID_INPUT")
            .details("Dados inválidos fornecidos")
            .build();

        assertNotNull(errorDTO);
        assertEquals(HttpStatus.BAD_REQUEST, errorDTO.getStatus());
        assertEquals("INVALID_INPUT", errorDTO.getReason());
        assertEquals("Dados inválidos fornecidos", errorDTO.getDetails());
    }

    @Test
    void shouldCreateErrorDTOWithConstructor() {
        ErrorDTO errorDTO = new ErrorDTO(
            HttpStatus.NOT_FOUND,
            "USER_NOT_FOUND",
            "Usuário não encontrado"
        );

        assertNotNull(errorDTO);
        assertEquals(HttpStatus.NOT_FOUND, errorDTO.getStatus());
        assertEquals("USER_NOT_FOUND", errorDTO.getReason());
        assertEquals("Usuário não encontrado", errorDTO.getDetails());
    }

    @Test
    void shouldCreateErrorDTOWithNoArgsConstructor() {
        ErrorDTO errorDTO = new ErrorDTO();

        assertNotNull(errorDTO);
    }

    @Test
    void shouldGetFormattedMessage() {
        ErrorDTO errorDTO = ErrorDTO.builder()
            .status(HttpStatus.UNAUTHORIZED)
            .reason("INVALID_TOKEN")
            .details("Token expirado")
            .build();

        String expectedMessage = "INVALID_TOKEN: Token expirado";
        assertEquals(expectedMessage, errorDTO.getMessage());
    }

    @Test
    void shouldSetAndGetStatus() {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorDTO.getStatus());
    }

    @Test
    void shouldSetAndGetReason() {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setReason("INTERNAL_ERROR");

        assertEquals("INTERNAL_ERROR", errorDTO.getReason());
    }

    @Test
    void shouldSetAndGetDetails() {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setDetails("Erro interno do servidor");

        assertEquals("Erro interno do servidor", errorDTO.getDetails());
    }
}

