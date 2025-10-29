package br.com.nimblebaas.payment_gateway.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.authentication.AuthenticationAction;
import br.com.nimblebaas.payment_gateway.events.authentication.LoginFailureEvent;
import br.com.nimblebaas.payment_gateway.events.authentication.LoginSuccessEvent;
import br.com.nimblebaas.payment_gateway.events.authentication.PasswordChangeEvent;
import br.com.nimblebaas.payment_gateway.events.authentication.RefreshTokenEvent;
import br.com.nimblebaas.payment_gateway.services.authentication.AuthenticationAuditService;

@ExtendWith(MockitoExtension.class)
class AuthenticationEventsListenerTest {

    @Mock
    private AuthenticationAuditService authenticationAuditService;

    @InjectMocks
    private AuthenticationEventsListener listener;

    private User user;
    private String cpfOrEmail;
    private String requestInfo;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setCpf("12345678900");
        user.setName("Test User");
        user.setEmail("test@email.com");

        cpfOrEmail = "12345678900";
        requestInfo = "IP: 127.0.0.1";
    }

    @Test
    void shouldHandleLoginSuccessEvent() {
        LoginSuccessEvent event = new LoginSuccessEvent(user, cpfOrEmail, requestInfo);

        listener.handleLoginSuccess(event);

        verify(authenticationAuditService, times(1)).registerAuditEvent(
            user,
            cpfOrEmail,
            requestInfo,
            AuthenticationAction.LOGIN_SUCCESS,
            true,
            "Login realizado com sucesso"
        );
    }

    @Test
    void shouldHandleLoginFailureEvent() {
        String reason = "Credenciais inválidas";
        LoginFailureEvent event = new LoginFailureEvent(cpfOrEmail, reason, requestInfo, null);

        listener.handleLoginFailure(event);

        verify(authenticationAuditService, times(1)).registerAuditEvent(
            isNull(),
            eq(cpfOrEmail),
            eq(requestInfo),
            eq(AuthenticationAction.LOGIN_FAILURE),
            eq(false),
            anyString()
        );
    }

    @Test
    void shouldHandleLoginFailureEventWithException() {
        String reason = "Token expirado";
        Exception exception = new RuntimeException("Token expired");
        LoginFailureEvent event = new LoginFailureEvent(cpfOrEmail, reason, requestInfo, exception);

        listener.handleLoginFailure(event);

        verify(authenticationAuditService, times(1)).registerAuditEvent(
            isNull(),
            eq(cpfOrEmail),
            eq(requestInfo),
            eq(AuthenticationAction.LOGIN_FAILURE),
            eq(false),
            anyString()
        );
    }

    @Test
    void shouldHandleRefreshTokenEventSuccess() {
        String message = "Token renovado com sucesso";
        RefreshTokenEvent event = new RefreshTokenEvent(user, cpfOrEmail, requestInfo, true, message);

        listener.handleRefreshToken(event);

        verify(authenticationAuditService, times(1)).registerAuditEvent(
            user,
            cpfOrEmail,
            requestInfo,
            AuthenticationAction.REFRESH_TOKEN,
            true,
            message
        );
    }

    @Test
    void shouldHandleRefreshTokenEventFailure() {
        String message = "Token inválido";
        RefreshTokenEvent event = new RefreshTokenEvent(user, cpfOrEmail, requestInfo, false, message);

        listener.handleRefreshToken(event);

        verify(authenticationAuditService, times(1)).registerAuditEvent(
            user,
            cpfOrEmail,
            requestInfo,
            AuthenticationAction.REFRESH_TOKEN,
            false,
            message
        );
    }

    @Test
    void shouldHandlePasswordChangeEvent() {
        PasswordChangeEvent event = new PasswordChangeEvent(user, cpfOrEmail, requestInfo);

        listener.handlePasswordChange(event);

        verify(authenticationAuditService, times(1)).registerAuditEvent(
            user,
            cpfOrEmail,
            requestInfo,
            AuthenticationAction.PASSWORD_CHANGE,
            true,
            "Senha alterada com sucesso"
        );
    }

    @Test
    void shouldHandleMultipleEvents() {
        LoginSuccessEvent loginEvent = new LoginSuccessEvent(user, cpfOrEmail, requestInfo);
        PasswordChangeEvent passwordEvent = new PasswordChangeEvent(user, cpfOrEmail, requestInfo);

        listener.handleLoginSuccess(loginEvent);
        listener.handlePasswordChange(passwordEvent);

        verify(authenticationAuditService, times(2)).registerAuditEvent(
            any(User.class),
            anyString(),
            anyString(),
            any(AuthenticationAction.class),
            anyBoolean(),
            anyString()
        );
    }
}

