package br.com.nimblebaas.payment_gateway.services.authentication;

import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.entities.authentication.AuthenticationAudit;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.authentication.AuthenticationAction;
import br.com.nimblebaas.payment_gateway.repositories.authentication.AuthenticationAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthenticationAuditService {

    private final AuthenticationAuditRepository authenticationAuditRepository;

    public void registerAuditEvent(
            User user, 
            String cpfOrEmail, 
            String ips,
            AuthenticationAction action, 
            Boolean success, 
            String message) {
        try {
            AuthenticationAudit audit = new AuthenticationAudit();
            audit.setUser(user);
            audit.setCpfOrEmail(cpfOrEmail);
            audit.setIps(ips);
            audit.setAction(action);
            audit.setSuccess(success);
            audit.setMessage(message);
            
            authenticationAuditRepository.save(audit);
            
            log.info("Evento de auditoria registrado: action={}, cpfOrEmail={}, success={}", 
                action, cpfOrEmail, success);
        } catch (Exception e) {
            log.error("Erro ao registrar auditoria: action={}, cpfOrEmail={}, success={}", 
                action, cpfOrEmail, success, e);
        }
    }

    public void registerAuditEvent(
            String cpfOrEmail, 
            String ips,
            AuthenticationAction action, 
            Boolean success, 
            String message) {
        registerAuditEvent(null, cpfOrEmail, ips, action, success, message);
    }
}

