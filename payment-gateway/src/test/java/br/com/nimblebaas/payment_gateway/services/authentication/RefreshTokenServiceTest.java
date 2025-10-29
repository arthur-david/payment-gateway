package br.com.nimblebaas.payment_gateway.services.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.nimblebaas.payment_gateway.entities.authentication.RefreshToken;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.authentication.TokenType;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.repositories.authentication.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiration", 86400L);

        user = new User();
        user.setCpf("12345678900");
        user.setEmail("john@example.com");

        refreshToken = new RefreshToken();
        refreshToken.setJti("test-jti-123");
        refreshToken.setUser(user);
        refreshToken.setIssuedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plusSeconds(86400));
        refreshToken.setUsed(false);
        refreshToken.setRevoked(false);
    }

    @Test
    void findByJtiAndUser_WithValidData_ShouldReturnRefreshToken() {
        when(refreshTokenRepository.findByJtiAndUser(anyString(), any(User.class)))
            .thenReturn(Optional.of(refreshToken));

        RefreshToken result = refreshTokenService.findByJtiAndUser("test-jti-123", user);

        assertNotNull(result);
        assertEquals("test-jti-123", result.getJti());
        verify(refreshTokenRepository).findByJtiAndUser("test-jti-123", user);
    }

    @Test
    void findByJtiAndUser_WithInvalidData_ShouldThrowException() {
        when(refreshTokenRepository.findByJtiAndUser(anyString(), any(User.class)))
            .thenReturn(Optional.empty());

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> refreshTokenService.findByJtiAndUser("invalid-jti", user)
        );

        assertEquals(BusinessRules.INVALID_TOKEN.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void validateAccessToken_WithValidToken_ShouldNotThrowException() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("tokenType", TokenType.ACCESS.name());
        claimsMap.put("email", user.getEmail());
        claimsMap.put("jti", "test-jti-123");
        Claims claims = new DefaultClaims(claimsMap);

        when(refreshTokenRepository.findByJtiAndUser(anyString(), any(User.class)))
            .thenReturn(Optional.of(refreshToken));

        refreshTokenService.validateAccessToken(claims, user);

        verify(refreshTokenRepository).findByJtiAndUser("test-jti-123", user);
    }

    @Test
    void validateAccessToken_WithInvalidTokenType_ShouldThrowException() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("tokenType", TokenType.REFRESH.name());
        Claims claims = new DefaultClaims(claimsMap);

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> refreshTokenService.validateAccessToken(claims, user)
        );

        assertEquals(BusinessRules.INVALID_TOKEN.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void validateAccessToken_WithRevokedToken_ShouldThrowException() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("tokenType", TokenType.ACCESS.name());
        claimsMap.put("email", user.getEmail());
        claimsMap.put("jti", "test-jti-123");
        Claims claims = new DefaultClaims(claimsMap);

        refreshToken.setRevoked(true);

        when(refreshTokenRepository.findByJtiAndUser(anyString(), any(User.class)))
            .thenReturn(Optional.of(refreshToken));

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> refreshTokenService.validateAccessToken(claims, user)
        );

        assertEquals(BusinessRules.INVALID_TOKEN.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void fetchAValidRefreshToken_WithValidToken_ShouldReturnRefreshToken() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("tokenType", TokenType.REFRESH.name());
        claimsMap.put("jti", "test-jti-123");
        Claims claims = new DefaultClaims(claimsMap);

        when(refreshTokenRepository.findByJtiAndUser(anyString(), any(User.class)))
            .thenReturn(Optional.of(refreshToken));

        RefreshToken result = refreshTokenService.fetchAValidRefreshToken(claims, user);

        assertNotNull(result);
        assertEquals("test-jti-123", result.getJti());
    }

    @Test
    void fetchAValidRefreshToken_WithInvalidTokenType_ShouldThrowException() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("tokenType", TokenType.ACCESS.name());
        Claims claims = new DefaultClaims(claimsMap);

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> refreshTokenService.fetchAValidRefreshToken(claims, user)
        );

        assertEquals(BusinessRules.INVALID_TOKEN.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void fetchAValidRefreshToken_WithUsedToken_ShouldThrowException() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("tokenType", TokenType.REFRESH.name());
        claimsMap.put("jti", "test-jti-123");
        Claims claims = new DefaultClaims(claimsMap);

        refreshToken.setUsed(true);

        when(refreshTokenRepository.findByJtiAndUser(anyString(), any(User.class)))
            .thenReturn(Optional.of(refreshToken));

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> refreshTokenService.fetchAValidRefreshToken(claims, user)
        );

        assertEquals(BusinessRules.INVALID_TOKEN.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void fetchAValidRefreshToken_WithExpiredToken_ShouldThrowException() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("tokenType", TokenType.REFRESH.name());
        claimsMap.put("jti", "test-jti-123");
        Claims claims = new DefaultClaims(claimsMap);

        refreshToken.setExpiresAt(Instant.now().minusSeconds(3600));

        when(refreshTokenRepository.findByJtiAndUser(anyString(), any(User.class)))
            .thenReturn(Optional.of(refreshToken));

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> refreshTokenService.fetchAValidRefreshToken(claims, user)
        );

        assertEquals(BusinessRules.INVALID_TOKEN.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void saveNewRefreshToken_ShouldSaveRefreshToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        refreshTokenService.saveNewRefreshToken(user, "new-jti-456");

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void saveAsUsedAndRevoked_ShouldUpdateRefreshToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        refreshTokenService.saveAsUsedAndRevoked(refreshToken);

        assertEquals(true, refreshToken.getUsed());
        assertEquals(true, refreshToken.getRevoked());
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    void revokeUserRefreshTokens_ShouldRevokeAllUserTokens() {
        RefreshToken token1 = new RefreshToken();
        token1.setRevoked(false);
        RefreshToken token2 = new RefreshToken();
        token2.setRevoked(false);
        
        List<RefreshToken> tokens = Arrays.asList(token1, token2);

        when(refreshTokenRepository.findByUserAndRevokedIsFalse(any(User.class))).thenReturn(tokens);
        when(refreshTokenRepository.saveAll(any())).thenReturn(tokens);

        refreshTokenService.revokeUserRefreshTokens(user);

        assertEquals(true, token1.getRevoked());
        assertEquals(true, token2.getRevoked());
        verify(refreshTokenRepository).saveAll(tokens);
    }
}

