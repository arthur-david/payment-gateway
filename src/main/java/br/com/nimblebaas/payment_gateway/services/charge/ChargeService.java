package br.com.nimblebaas.payment_gateway.services.charge;

import static java.util.Objects.isNull;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.configs.authentication.UserAuthenticated;
import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargeInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.charge.ChargeOutputRecord;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.repositories.charge.ChargeRepository;
import br.com.nimblebaas.payment_gateway.services.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChargeService {
    
    private final ChargeRepository chargeRepository;
    private final UserService userService;

    public ChargeOutputRecord create(UserAuthenticated userAuthenticated, @Valid ChargeInputRecord chargeInputRecord) {
        var destinationCpf = chargeInputRecord.getDestinationCpfOnlyNumbers();

        if (destinationCpf.equals(userAuthenticated.getUser().getCpf()))
            throw new BusinessRuleException(
                getClass(), 
                BusinessRules.CHARGE_DESTINATION_SAME_AS_ORIGINATOR, 
                "O CPF de destino não pode ser o mesmo do originador"
            );
        
        var destinationUser = userService.getUserByCpf(destinationCpf);

        var charge = Charge.builder()
            .originatorUser(userAuthenticated.getUser())
            .destinationUser(destinationUser)
            .amount(chargeInputRecord.amount())
            .description(chargeInputRecord.description())
            .status(ChargeStatus.PENDING)
            .build();

        chargeRepository.save(charge);

        return new ChargeOutputRecord(charge);
    }

    public List<ChargeOutputRecord> getSentChargesByUser(UserAuthenticated userAuthenticated, List<ChargeStatus> statuses) {
        if (isNull(statuses) || statuses.isEmpty())
            statuses = List.of(ChargeStatus.PENDING, ChargeStatus.PAID, ChargeStatus.CANCELLED);

        return chargeRepository.findByOriginatorUserAndStatusIn(userAuthenticated.getUser(), statuses)
            .stream().map(ChargeOutputRecord::new)
            .toList();
    }

    public List<ChargeOutputRecord> getReceivedChargesByUser(UserAuthenticated userAuthenticated, List<ChargeStatus> statuses) {
        if (isNull(statuses) || statuses.isEmpty())
            statuses = List.of(ChargeStatus.PENDING, ChargeStatus.PAID, ChargeStatus.CANCELLED);

        return chargeRepository.findByDestinationUserAndStatusIn(userAuthenticated.getUser(), statuses)
            .stream().map(ChargeOutputRecord::new)
            .toList();
    }
}
