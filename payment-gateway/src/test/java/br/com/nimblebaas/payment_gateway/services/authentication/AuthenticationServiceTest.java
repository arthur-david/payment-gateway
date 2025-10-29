package br.com.nimblebaas.payment_gateway.services.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.nimblebaas.payment_gateway.dtos.input.authentication.LoginRequestRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.authentication.LoginResponseRecord;
import br.com.nimblebaas.payment_gateway.entities.authentication.RefreshToken;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.authentication.TokenType;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.events.authentication.LoginFailureEvent;
import br.com.nimblebaas.payment_gateway.events.authentication.LoginSuccessEvent;
import br.com.nimblebaas.payment_gateway.events.authentication.RefreshTokenEvent;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.services.user.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthenticationService authenticationService;

    private LoginRequestRecord loginRequest;
    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "accessTokenExpiration", 3600L);

        loginRequest = new LoginRequestRecord("12345678900", "password123");

        user = new User();
        user.setCpf("12345678900");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");

        refreshToken = new RefreshToken();
        refreshToken.setJti("test-jti-123");
        refreshToken.setUser(user);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader(any())).thenReturn("Mozilla/5.0");
        when(userService.findByCpfOrEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        doNothing().when(refreshTokenService).revokeUserRefreshTokens(any(User.class));
        when(jwtService.generateAccessToken(any(User.class), anyString())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class), anyString())).thenReturn("refresh-token");
        doNothing().when(refreshTokenService).saveNewRefreshToken(any(User.class), anyString());

        LoginResponseRecord response = authenticationService.login(loginRequest);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals(3600L, response.expiresIn());
        verify(eventPublisher).publishEvent(any(LoginSuccessEvent.class));
    }

    @Test
    void login_WithInvalidCpfOrEmail_ShouldThrowException() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader(any())).thenReturn("Mozilla/5.0");
        when(userService.findByCpfOrEmail(anyString())).thenReturn(Optional.empty());

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> authenticationService.login(loginRequest)
        );

        assertEquals(BusinessRules.INVALID_CREDENTIALS.name(), exception.getErrorDTO().getReason());
        verify(eventPublisher).publishEvent(any(LoginFailureEvent.class));
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader(any())).thenReturn("Mozilla/5.0");
        when(userService.findByCpfOrEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> authenticationService.login(loginRequest)
        );

        assertEquals(BusinessRules.INVALID_CREDENTIALS.name(), exception.getErrorDTO().getReason());
        verify(eventPublisher).publishEvent(any(LoginFailureEvent.class));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader(any())).thenReturn("Mozilla/5.0");
        
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("tokenType", TokenType.REFRESH.name());
        claimsMap.put("sub", "12345678900");
        claimsMap.put("jti", "test-jti-123");
        Claims claims = new DefaultClaims(claimsMap);

        when(jwtService.parseToken(anyString())).thenReturn(claims);
        when(userService.findByCpf(anyString())).thenReturn(Optional.of(user));
        when(refreshTokenService.fetchAValidRefreshToken(any(Claims.class), any(User.class)))
            .thenReturn(refreshToken);
        doNothing().when(refreshTokenService).saveAsUsedAndRevoked(any(RefreshToken.class));
        when(jwtService.generateAccessToken(any(User.class), anyString())).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(any(User.class), anyString())).thenReturn("new-refresh-token");
        doNothing().when(refreshTokenService).saveNewRefreshToken(any(User.class), anyString());

        LoginResponseRecord response = authenticationService.refreshToken("old-refresh-token");

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertEquals("new-refresh-token", response.refreshToken());
        verify(refreshTokenService).saveAsUsedAndRevoked(refreshToken);
        verify(eventPublisher).publishEvent(any(RefreshTokenEvent.class));
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowException() {
        when(jwtService.parseToken(anyString())).thenThrow(new RuntimeException("Invalid token"));

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authenticationService.refreshToken("invalid-token")
        );

        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    void refreshToken_WithNonExistentUser_ShouldThrowException() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader(any())).thenReturn("Mozilla/5.0");
        
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("tokenType", TokenType.REFRESH.name());
        claimsMap.put("sub", "99999999999");
        claimsMap.put("jti", "test-jti-123");
        Claims claims = new DefaultClaims(claimsMap);

        when(jwtService.parseToken(anyString())).thenReturn(claims);
        when(userService.findByCpf(anyString())).thenReturn(Optional.empty());

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> authenticationService.refreshToken("refresh-token")
        );

        assertEquals(BusinessRules.USER_NOT_FOUND.name(), exception.getErrorDTO().getReason());
        verify(eventPublisher).publishEvent(any(RefreshTokenEvent.class));
    }
}

