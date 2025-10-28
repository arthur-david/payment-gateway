package br.com.nimblebaas.payment_gateway.repositories.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.nimblebaas.payment_gateway.entities.transaction.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
