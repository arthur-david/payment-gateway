package br.com.nimblebaas.payment_gateway.repositories.authentication;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.nimblebaas.payment_gateway.entities.authentication.AuthenticationAudit;

public interface AuthenticationAuditRepository extends JpaRepository<AuthenticationAudit, Long> {
    
}

