package br.com.nimblebaas.payment_gateway.exceptions;

import static java.util.Objects.nonNull;

import org.springframework.http.HttpStatus;

import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import lombok.Getter;

public class BusinessRuleException extends RuntimeException {

    private static final long serialVersionUID = 4739823478237432101L;

    @Getter
    private final ErrorDTO errorDTO;

    @Getter
    private final Class<?> source;

    @Override
    public String getMessage() {
        return nonNull(errorDTO) ? errorDTO.getMessage() : null;
    }

    public BusinessRuleException(Class<?> source, BusinessRules reason, String details, Object... args) {
        this(HttpStatus.BAD_REQUEST, source, reason, details, args);
    }

    public BusinessRuleException(HttpStatus status, Class<?> source, BusinessRules reason, String details, Object... args) {
        this(status, source, reason, String.format(details, args));
    }

    public BusinessRuleException(Class<?> source, BusinessRules reason, String details) {
        this(HttpStatus.BAD_REQUEST, source, reason, details);
    }

    public BusinessRuleException(HttpStatus status, Class<?> source, BusinessRules reason, String details) {
        this.source = source;
        this.errorDTO = ErrorDTO.builder()
            .status(status)
            .reason(reason.name())
            .details(details)
            .build();
    }
}
