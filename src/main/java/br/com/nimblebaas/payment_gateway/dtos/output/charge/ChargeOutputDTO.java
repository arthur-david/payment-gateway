package br.com.nimblebaas.payment_gateway.dtos.output.charge;

import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChargeOutputDTO {
    private String identifier;
    private String originatorCpf;
    private String destinationCpf;
    private BigDecimal amount;
    private String description;
    private ChargeStatus status;
    private LocalDateTime createdAt;
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