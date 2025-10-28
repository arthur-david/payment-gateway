package br.com.nimblebaas.payment_gateway.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorDTO> handleBusinessRuleException(BusinessRuleException ex, WebRequest request) {
        log.error("BusinessRuleException: {} - Source: {}", ex.getMessage(), ex.getSource(), ex);
        
        ErrorDTO errorDTO = ex.getErrorDTO();
        return ResponseEntity
            .status(errorDTO.getStatus())
            .body(errorDTO);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error: {} - Source: {}", ex.getMessage(), ex.getClass(), ex);
        
        StringBuilder details = new StringBuilder("Erros de validação: ");
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            details.append(String.format("[%s: %s] ", error.getField(), error.getDefaultMessage()))
        );
        
        ErrorDTO errorDTO = ErrorDTO.builder()
            .status(HttpStatus.BAD_REQUEST)
            .reason("VALIDATION_ERROR")
            .details(details.toString())
            .build();
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorDTO);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDTO> handleTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.error("Type mismatch error: {} - Source: {}", ex.getMessage(), ex.getClass(), ex);
        
        String typeName = "desconhecido";
        Class<?> requiredType = ex.getRequiredType();
        if (requiredType != null) {
            typeName = requiredType.getSimpleName();
        }
        
        String details = String.format("O parâmetro '%s' com valor '%s' não pôde ser convertido para o tipo %s", 
            ex.getName(), 
            ex.getValue(), 
            typeName);
        
        ErrorDTO errorDTO = ErrorDTO.builder()
            .status(HttpStatus.BAD_REQUEST)
            .reason("TYPE_MISMATCH")
            .details(details)
            .build();
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorDTO);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument: {} - Source: {}", ex.getMessage(), ex.getClass(), ex);
        
        ErrorDTO errorDTO = ErrorDTO.builder()
            .status(HttpStatus.BAD_REQUEST)
            .reason("ILLEGAL_ARGUMENT")
            .details(ex.getMessage())
            .build();
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGenericException(Exception ex, WebRequest request) {

        if (ex instanceof NoResourceFoundException) {

            String details = String.format("O recurso %s %s não foi encontrado", request.getDescription(false), request.getContextPath());
            ErrorDTO errorDTO = ErrorDTO.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason("NOT_FOUND")
                .details(details)
                .build();

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDTO);
        }

        log.error("Unexpected error: {} - Source: {}", ex.getMessage(), ex.getClass(), ex);
        
        ErrorDTO errorDTO = ErrorDTO.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .reason("INTERNAL_ERROR")
            .details("Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.")
            .build();
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorDTO);
    }
}

