package br.com.nimblebaas.payment_gateway.repositories.account;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.nimblebaas.payment_gateway.entities.account.HoldBalance;

public interface HoldBalanceRepository extends JpaRepository<HoldBalance, Long> {
    
}
