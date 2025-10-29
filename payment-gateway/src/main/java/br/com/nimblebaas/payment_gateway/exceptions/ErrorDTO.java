package br.com.nimblebaas.payment_gateway.exceptions;

import java.io.Serializable;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO implements Serializable {
    
    private static final long serialVersionUID = 4739823478237432102L;
    
    private HttpStatus status;
    private String reason;
    private String details;

    public String getMessage() {
        return String.format("%s: %s", reason, details);
    }
}
