package br.com.nimblebaas.payment_gateway.services.authentication;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.entities.authentication.RefreshToken;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.authentication.TokenType;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.repositories.authentication.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.security.jwt.expiration.refresh}")
    private Long refreshTokenExpiration;

    public RefreshToken findByJtiAndUser(String jti, User user) {
        return refreshTokenRepository.findByJtiAndUser(jti, user)
            .orElseThrow(() -> new BusinessRuleException(
                HttpStatus.UNAUTHORIZED,
                getClass(),
                BusinessRules.INVALID_TOKEN,
                "Token inválido"
            ));
    }

    public void validateAccessToken(Claims claims, User user) {
        if (!TokenType.ACCESS.name().equals(claims.get("tokenType"))) {
            throw new BusinessRuleException(
                HttpStatus.UNAUTHORIZED,
                getClass(),
                BusinessRules.INVALID_TOKEN,
                "Access token inválido"
            );
        }

        var email = claims.get("email");
        if (!user.getEmail().equals(email)) {
            throw new BusinessRuleException(
                HttpStatus.UNAUTHORIZED,
                getClass(),
                BusinessRules.INVALID_TOKEN,
                "Access token inválido"
            );
        }

        RefreshToken refreshToken = findByJtiAndUser(claims.getId(), user);

        if (isTrue(refreshToken.getRevoked())) {
            throw new BusinessRuleException(
                HttpStatus.UNAUTHORIZED,
                getClass(),
                BusinessRules.INVALID_TOKEN,
                "Token revogado, por favor faça login novamente"
            );
        }
    }

    public RefreshToken fetchAValidRefreshToken(Claims claims, User user) {
        if (!TokenType.REFRESH.name().equals(claims.get("tokenType"))) {
            throw new BusinessRuleException(
                HttpStatus.UNAUTHORIZED,
                getClass(),
                BusinessRules.INVALID_TOKEN,
                "Refresh token inválido"
            );
        }

        String jti = claims.getId();

        RefreshToken refreshToken = findByJtiAndUser(jti, user);

        if (refreshToken.isUsedOrRevoked()) {
            throw new BusinessRuleException(
                HttpStatus.UNAUTHORIZED,
                getClass(),
                BusinessRules.INVALID_TOKEN,
                "Refresh token já foi usado ou revogado, por favor faça login novamente"
            );
        }

        if (refreshToken.isExpired()) {
            throw new BusinessRuleException(
                HttpStatus.UNAUTHORIZED,
                getClass(),
                BusinessRules.INVALID_TOKEN,
                "Refresh token expirado, por favor faça login novamente"
            );
        }

        return refreshToken;
    }

    public void saveNewRefreshToken(User user, String jti) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setJti(jti);
        refreshToken.setUser(user);
        refreshToken.setIssuedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plusSeconds(refreshTokenExpiration));
        
        refreshTokenRepository.save(refreshToken);
    }

    public void saveAsUsedAndRevoked(RefreshToken refreshToken) {
        refreshToken.setUsed(true);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    public void revokeUserRefreshTokens(User user) {
        List<RefreshToken> refreshTokens = refreshTokenRepository.findByUserAndRevokedIsFalse(user);
        refreshTokens.forEach(refreshToken -> refreshToken.setRevoked(true));
        refreshTokenRepository.saveAll(refreshTokens);
    }
}
