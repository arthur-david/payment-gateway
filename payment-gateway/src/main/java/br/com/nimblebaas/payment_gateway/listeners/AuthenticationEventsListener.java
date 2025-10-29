package br.com.nimblebaas.payment_gateway.listeners;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import br.com.nimblebaas.payment_gateway.enums.authentication.AuthenticationAction;
import br.com.nimblebaas.payment_gateway.events.authentication.LoginFailureEvent;
import br.com.nimblebaas.payment_gateway.events.authentication.LoginSuccessEvent;
import br.com.nimblebaas.payment_gateway.events.authentication.PasswordChangeEvent;
import br.com.nimblebaas.payment_gateway.events.authentication.RefreshTokenEvent;
import br.com.nimblebaas.payment_gateway.services.authentication.AuthenticationAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationEventsListener {

    private final AuthenticationAuditService authenticationAuditService;

    @Async
    @EventListener
    public void handleLoginSuccess(LoginSuccessEvent event) {
        log.debug("Processando evento de login bem-sucedido para: {}", event.getCpfOrEmail());
        
        authenticationAuditService.registerAuditEvent(
            event.getUser(),
            event.getCpfOrEmail(),
            event.getRequestInfo(),
            AuthenticationAction.LOGIN_SUCCESS,
            true,
            "Login realizado com sucesso"
        );
    }

    @Async
    @EventListener
    public void handleLoginFailure(LoginFailureEvent event) {
        log.debug("Processando evento de login falho para: {}", event.getCpfOrEmail());
        
        String message = String.format("Tentativa de login falhou: %s", event.getReason());
        if (event.getException() != null) {
            message += String.format(" - Exception: %s", event.getException().getClass().getSimpleName());
        }
        
        authenticationAuditService.registerAuditEvent(
            null,
            event.getCpfOrEmail(),
            event.getRequestInfo(),
            AuthenticationAction.LOGIN_FAILURE,
            false,
            message
        );
    }

    @Async
    @EventListener
    public void handleRefreshToken(RefreshTokenEvent event) {
        log.debug("Processando evento de refresh token para: {}", event.getCpfOrEmail());
        
        authenticationAuditService.registerAuditEvent(
            event.getUser(),
            event.getCpfOrEmail(),
            event.getRequestInfo(),
            AuthenticationAction.REFRESH_TOKEN,
            event.isSuccess(),
            event.getMessage()
        );
    }

    @Async
    @EventListener
    public void handlePasswordChange(PasswordChangeEvent event) {
        log.debug("Processando evento de alteração de senha para: {}", event.getCpfOrEmail());
        
        authenticationAuditService.registerAuditEvent(
            event.getUser(),
            event.getCpfOrEmail(),
            event.getRequestInfo(),
            AuthenticationAction.PASSWORD_CHANGE,
            true,
            "Senha alterada com sucesso"
        );
    }
}

