package br.com.nimblebaas.payment_gateway.dtos.output.charge;

import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados completos de uma cobrança")
public class ChargeOutputDTO {
    @Schema(description = "Identificador único da cobrança", example = "550e8400-e29b-41d4-a716-446655440000")
    private String identifier;
    
    @Schema(description = "CPF do criador da cobrança", example = "12345678900")
    private String originatorCpf;
    
    @Schema(description = "CPF do destinatário da cobrança", example = "98765432100")
    private String destinationCpf;
    
    @Schema(description = "Valor da cobrança em reais", example = "50.00")
    private BigDecimal amount;
    
    @Schema(description = "Descrição da cobrança", example = "Pagamento de serviço prestado")
    private String description;
    
    @Schema(description = "Status da cobrança (PENDING, PAID, CANCELLED)", example = "PENDING")
    private ChargeStatus status;
    
    @Schema(description = "Data e hora de criação da cobrança", example = "2025-10-28T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Informações do pagamento (se já foi paga)")
    private ChargePaymentOutputDTO payment;

    public ChargeOutputDTO(Charge charge) {
        setIdentifier(charge.getIdentifier());
        setOriginatorCpf(charge.getOriginatorUser().getCpf());
        setDestinationCpf(charge.getDestinationUser().getCpf());
        setAmount(charge.getAmount());
        setDescription(charge.getDescription());
        setStatus(charge.getStatus());
        setCreatedAt(charge.getCreatedAt());

        if (nonNull(charge.getPayment()))
            setPayment(new ChargePaymentOutputDTO(charge.getPayment()));
    }
}