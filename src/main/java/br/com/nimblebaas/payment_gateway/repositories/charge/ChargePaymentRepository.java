package br.com.nimblebaas.payment_gateway.repositories.charge;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.nimblebaas.payment_gateway.entities.charge.ChargePayment;

public interface ChargePaymentRepository extends JpaRepository<ChargePayment, Long> {

}
