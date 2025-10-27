package br.com.nimblebaas.payment_gateway.repositories.charge;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;

public interface ChargeRepository extends JpaRepository<Charge, Long> {

    List<Charge> findByOriginatorUserAndStatusIn(User user, List<ChargeStatus> statuses);
    
    List<Charge> findByDestinationUserAndStatusIn(User user, List<ChargeStatus> statuses);
}
