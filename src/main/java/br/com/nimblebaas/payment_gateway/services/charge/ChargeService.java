package br.com.nimblebaas.payment_gateway.services.charge;

import static java.util.Objects.isNull;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.configs.authentication.UserAuthenticated;
import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargeInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargePaymentInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.internal.charge.ChargePaymentDTO;
import br.com.nimblebaas.payment_gateway.dtos.output.charge.ChargeOutputDTO;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.repositories.charge.ChargeRepository;
import br.com.nimblebaas.payment_gateway.services.charge.payment.ChargePaymentService;
import br.com.nimblebaas.payment_gateway.services.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChargeService {
    
    private final ChargeRepository chargeRepository;
    private final UserService userService;
    private final ChargePaymentService chargePaymentService;

    public ChargeOutputDTO create(UserAuthenticated userAuthenticated, @Valid ChargeInputRecord chargeInputRecord) {
        var destinationCpf = chargeInputRecord.getDestinationCpfOnlyNumbers();

        if (destinationCpf.equals(userAuthenticated.getUser().getCpf()))
            throw new BusinessRuleException(
                getClass(), 
                BusinessRules.CHARGE_DESTINATION_SAME_AS_ORIGINATOR, 
                "O CPF de destino não pode ser o mesmo do originador"
            );
        
        var destinationUser = userService.getUserByCpf(destinationCpf);

        var charge = Charge.builder()
            .identifier(UUID.randomUUID().toString())
            .originatorUser(userAuthenticated.getUser())
            .destinationUser(destinationUser)
            .amount(chargeInputRecord.amount())
            .description(chargeInputRecord.description())
            .status(ChargeStatus.PENDING)
            .build();

        chargeRepository.save(charge);

        return new ChargeOutputDTO(charge);
    }

    public List<ChargeOutputDTO> getSentChargesByUser(UserAuthenticated userAuthenticated, List<ChargeStatus> statuses) {
        if (isNull(statuses) || statuses.isEmpty())
            statuses = List.of(ChargeStatus.PENDING, ChargeStatus.PAID, ChargeStatus.CANCELLED);

        return chargeRepository.findByOriginatorUserAndStatusIn(userAuthenticated.getUser(), statuses)
            .stream().map(ChargeOutputDTO::new)
            .toList();
    }

    public List<ChargeOutputDTO> getReceivedChargesByUser(UserAuthenticated userAuthenticated, List<ChargeStatus> statuses) {
        if (isNull(statuses) || statuses.isEmpty())
            statuses = List.of(ChargeStatus.PENDING, ChargeStatus.PAID, ChargeStatus.CANCELLED);

        return chargeRepository.findByDestinationUserAndStatusIn(userAuthenticated.getUser(), statuses)
            .stream().map(ChargeOutputDTO::new)
            .toList();
    }

    public void pay(UserAuthenticated userAuthenticated, @Valid ChargePaymentInputRecord chargePaymentInputRecord) {
        var charge = chargeRepository.findByIdentifier(chargePaymentInputRecord.identifier())
            .orElseThrow(() -> new BusinessRuleException(
                getClass(), 
                BusinessRules.CHARGE_NOT_FOUND, 
                "Cobrança não encontrada"
            ));

        if (!charge.getDestinationUser().getCpf().equals(userAuthenticated.getUser().getCpf()))
            throw new BusinessRuleException(
                getClass(), 
                BusinessRules.CHARGE_NOT_ALLOWED_TO_PAY, 
                "Você não é o destinatário da cobrança"
            );

        try {
            chargePaymentService.pay(new ChargePaymentDTO(chargePaymentInputRecord, charge), chargePaymentInputRecord.paymentMethod());
        } catch (BusinessRuleException e) {
            charge.setStatus(ChargeStatus.PAYMENT_FAILED);
            charge.setErrorMessage(e.getErrorDTO().getDetails());
            chargeRepository.save(charge);
            throw e;
        } catch (Exception e) {
            charge.setStatus(ChargeStatus.PAYMENT_FAILED);
            charge.setErrorMessage(e.getMessage());
            chargeRepository.save(charge);
            throw new BusinessRuleException(
                getClass(), 
                BusinessRules.CHARGE_PAYMENT_ERROR, 
                "Erro ao pagar a cobrança"
            );
        }

        charge.setStatus(ChargeStatus.PAID);
        charge.setErrorMessage(null);
        chargeRepository.save(charge);
    }
}
