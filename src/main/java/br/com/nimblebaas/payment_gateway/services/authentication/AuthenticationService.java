package br.com.nimblebaas.payment_gateway.services.authentication;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.dtos.input.authentication.LoginRequestRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.authentication.LoginResponseRecord;
import br.com.nimblebaas.payment_gateway.entities.authentication.RefreshToken;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.services.user.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.security.jwt.expiration.access}")
    private Long accessTokenExpiration;

    public LoginResponseRecord login(LoginRequestRecord loginRequest) {
        User user = userService.findByCpfOrEmail(loginRequest.cpfOrEmail())
            .orElseThrow(() -> new BusinessRuleException(
                HttpStatus.UNAUTHORIZED,
                getClass(),
                BusinessRules.INVALID_CREDENTIALS,
                "CPF/e-mail ou senha inválidos"
            ));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new BusinessRuleException(
                HttpStatus.UNAUTHORIZED,
                getClass(),
                BusinessRules.INVALID_CREDENTIALS,
                "CPF/e-mail ou senha inválidos"
            );
        }

        refreshTokenService.revokeUserRefreshTokens(user);

        String jti = UUID.randomUUID().toString();
        String accessToken = jwtService.generateAccessToken(user, jti);
        String refreshTokenString = jwtService.generateRefreshToken(user, jti);

        refreshTokenService.saveNewRefreshToken(user, jti);

        return new LoginResponseRecord(accessToken, refreshTokenString, accessTokenExpiration);
    }

    public LoginResponseRecord refreshToken(String refreshTokenString) {
        Claims claims = jwtService.parseToken(refreshTokenString);

        String cpf = claims.getSubject();
        User user = userService.findByCpf(cpf)
            .orElseThrow(() -> new BusinessRuleException(
                HttpStatus.UNAUTHORIZED,
                getClass(),
                BusinessRules.USER_NOT_FOUND,
                "Usuário não encontrado"
            ));
        
        RefreshToken refreshToken = refreshTokenService.fetchAValidRefreshToken(claims, user);
        refreshTokenService.saveAsUsedAndRevoked(refreshToken);

        String newJti = UUID.randomUUID().toString();
        String newAccessToken = jwtService.generateAccessToken(user, newJti);
        String newRefreshToken = jwtService.generateRefreshToken(user, newJti);

        refreshTokenService.saveNewRefreshToken(user, newJti);

        return new LoginResponseRecord(newAccessToken, newRefreshToken, accessTokenExpiration);
    }
}
