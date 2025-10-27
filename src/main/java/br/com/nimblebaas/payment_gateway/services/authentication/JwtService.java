package br.com.nimblebaas.payment_gateway.services.authentication;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.authentication.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    
    @Value("${app.security.jwt.secret}")
    private String secret;

    @Value("${app.security.jwt.expiration.access}")
    private Long accessTokenExpiration;

    @Value("${app.security.jwt.expiration.refresh}")
    private Long refreshTokenExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user, String jti) {
        Instant now = Instant.now();
        
        return Jwts.builder()
            .id(jti)
            .subject(user.getCpf())
            .claim("email", user.getEmail())
            .claim("tokenType", TokenType.ACCESS.name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(accessTokenExpiration)))
            .signWith(getSigningKey())
            .compact();
    }

    public String generateRefreshToken(User user, String jti) {
        Instant now = Instant.now();

        return Jwts.builder()
            .id(jti)
            .subject(user.getCpf())
            .claim("tokenType", TokenType.REFRESH.name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(refreshTokenExpiration)))
            .signWith(getSigningKey())
            .compact();
    }
    
    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
