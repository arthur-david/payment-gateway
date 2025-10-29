package br.com.nimblebaas.payment_gateway.repositories.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.nimblebaas.payment_gateway.entities.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByCpfOrEmail(String cpf, String email);

    Optional<User> findByCpfOrEmail(String cpf, String email);

    Optional<User> findByCpf(String cpf);
}
