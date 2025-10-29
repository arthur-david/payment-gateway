package br.com.nimblebaas.payment_gateway.services.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.authentication.TokenType;
import io.jsonwebtoken.Claims;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "my-super-secret-key-for-testing-purposes-minimum-256-bits");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3600L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 86400L);

        user = new User();
        user.setCpf("12345678900");
        user.setEmail("john@example.com");
    }

    @Test
    void generateAccessToken_ShouldReturnValidToken() {
        String jti = "test-jti-123";

        String token = jwtService.generateAccessToken(user, jti);

        assertNotNull(token);
        Claims claims = jwtService.parseToken(token);
        assertEquals(jti, claims.getId());
        assertEquals(user.getCpf(), claims.getSubject());
        assertEquals(user.getEmail(), claims.get("email"));
        assertEquals(TokenType.ACCESS.name(), claims.get("tokenType"));
    }

    @Test
    void generateRefreshToken_ShouldReturnValidToken() {
        String jti = "test-refresh-jti-456";

        String token = jwtService.generateRefreshToken(user, jti);

        assertNotNull(token);
        Claims claims = jwtService.parseToken(token);
        assertEquals(jti, claims.getId());
        assertEquals(user.getCpf(), claims.getSubject());
        assertEquals(TokenType.REFRESH.name(), claims.get("tokenType"));
    }

    @Test
    void parseToken_ShouldReturnValidClaims() {
        String jti = "test-parse-jti-789";
        String token = jwtService.generateAccessToken(user, jti);

        Claims claims = jwtService.parseToken(token);

        assertNotNull(claims);
        assertEquals(jti, claims.getId());
        assertEquals(user.getCpf(), claims.getSubject());
        assertEquals(user.getEmail(), claims.get("email"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
}

