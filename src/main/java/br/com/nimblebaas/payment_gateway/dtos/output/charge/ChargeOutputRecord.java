package br.com.nimblebaas.payment_gateway.dtos.output.charge;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;

public record ChargeOutputRecord(
    String originatorCpf,
    String destinationCpf,
    BigDecimal amount,
    String description,
    ChargeStatus status,
    LocalDateTime createdAt,
    LocalDateTime paidAt,
    LocalDateTime cancelledAt
) {
    public ChargeOutputRecord(Charge charge) {
        this(
            charge.getOriginatorUser().getCpf(), 
            charge.getDestinationUser().getCpf(), 
            charge.getAmount(), 
            charge.getDescription(), 
            charge.getStatus(),
            charge.getCreatedAt(),
            charge.getPaidAt(),
            charge.getCancelledAt()
        );
    }
}