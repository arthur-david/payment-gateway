package br.com.nimblebaas.payment_gateway.repositories.authentication;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.nimblebaas.payment_gateway.entities.authentication.RefreshToken;
import br.com.nimblebaas.payment_gateway.entities.user.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByJtiAndUser(String jti, User user);

    List<RefreshToken> findByUserAndRevokedIsFalse(User user);
}

