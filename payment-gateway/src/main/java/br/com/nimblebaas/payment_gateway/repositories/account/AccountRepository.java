package br.com.nimblebaas.payment_gateway.repositories.account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.user.User;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUser(User user);
}
