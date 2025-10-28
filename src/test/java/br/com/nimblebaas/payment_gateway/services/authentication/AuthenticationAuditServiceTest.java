package br.com.nimblebaas.payment_gateway.services.authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.nimblebaas.payment_gateway.entities.authentication.AuthenticationAudit;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.authentication.AuthenticationAction;
import br.com.nimblebaas.payment_gateway.repositories.authentication.AuthenticationAuditRepository;

@ExtendWith(MockitoExtension.class)
class AuthenticationAuditServiceTest {

    @Mock
    private AuthenticationAuditRepository authenticationAuditRepository;

    @InjectMocks
    private AuthenticationAuditService authenticationAuditService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setCpf("12345678900");
        user.setEmail("john@example.com");
    }

    @Test
    void registerAuditEvent_WithUser_ShouldSaveAudit() {
        when(authenticationAuditRepository.save(any(AuthenticationAudit.class)))
            .thenReturn(new AuthenticationAudit());

        authenticationAuditService.registerAuditEvent(
            user,
            "12345678900",
            "127.0.0.1",
            AuthenticationAction.LOGIN_SUCCESS,
            true,
            "Login bem-sucedido"
        );

        verify(authenticationAuditRepository).save(any(AuthenticationAudit.class));
    }

    @Test
    void registerAuditEvent_WithoutUser_ShouldSaveAudit() {
        when(authenticationAuditRepository.save(any(AuthenticationAudit.class)))
            .thenReturn(new AuthenticationAudit());

        authenticationAuditService.registerAuditEvent(
            "12345678900",
            "127.0.0.1",
            AuthenticationAction.LOGIN_SUCCESS,
            false,
            "Credenciais inválidas"
        );

        verify(authenticationAuditRepository).save(any(AuthenticationAudit.class));
    }

    @Test
    void registerAuditEvent_WhenExceptionOccurs_ShouldNotThrow() {
        when(authenticationAuditRepository.save(any(AuthenticationAudit.class)))
            .thenThrow(new RuntimeException("Database error"));

        authenticationAuditService.registerAuditEvent(
            user,
            "12345678900",
            "127.0.0.1",
            AuthenticationAction.LOGIN_SUCCESS,
            true,
            "Login bem-sucedido"
        );

        verify(authenticationAuditRepository).save(any(AuthenticationAudit.class));
    }
}

