package br.com.nimblebaas.payment_gateway.services.authentication;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.dtos.input.authentication.LoginRequestRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.authentication.LoginResponseRecord;
import br.com.nimblebaas.payment_gateway.entities.authentication.RefreshToken;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.events.authentication.LoginFailureEvent;
import br.com.nimblebaas.payment_gateway.events.authentication.LoginSuccessEvent;
import br.com.nimblebaas.payment_gateway.events.authentication.RefreshTokenEvent;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.helpers.HttpRequestHelper;
import br.com.nimblebaas.payment_gateway.services.user.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final HttpServletRequest httpServletRequest;

    @Value("${app.security.jwt.expiration.access}")
    private Long accessTokenExpiration;

    public LoginResponseRecord login(LoginRequestRecord loginRequest) {
        String requestInfo = HttpRequestHelper.formatRequestInfo(httpServletRequest);
        
        try {
            User user = userService.findByCpfOrEmail(loginRequest.cpfOrEmail())
                .orElseThrow(() -> {
                    BusinessRuleException ex = new BusinessRuleException(
                        HttpStatus.UNAUTHORIZED,
                        getClass(),
                        BusinessRules.INVALID_CREDENTIALS,
                        "CPF/e-mail ou senha inválidos"
                    );
                    
                    eventPublisher.publishEvent(new LoginFailureEvent(
                        loginRequest.cpfOrEmail(),
                        "Usuário não encontrado",
                        requestInfo,
                        ex
                    ));
                    
                    throw ex;
                });

            if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
                BusinessRuleException ex = new BusinessRuleException(
                    HttpStatus.UNAUTHORIZED,
                    getClass(),
                    BusinessRules.INVALID_CREDENTIALS,
                    "CPF/e-mail ou senha inválidos"
                );
                
                eventPublisher.publishEvent(new LoginFailureEvent(
                    loginRequest.cpfOrEmail(),
                    "Senha incorreta",
                    requestInfo,
                    ex
                ));
                
                throw ex;
            }

            refreshTokenService.revokeUserRefreshTokens(user);

            String jti = UUID.randomUUID().toString();
            String accessToken = jwtService.generateAccessToken(user, jti);
            String refreshTokenString = jwtService.generateRefreshToken(user, jti);

            refreshTokenService.saveNewRefreshToken(user, jti);

            eventPublisher.publishEvent(new LoginSuccessEvent(
                user,
                user.getCpf(),
                requestInfo
            ));

            return new LoginResponseRecord(accessToken, refreshTokenString, accessTokenExpiration);
        } catch (BusinessRuleException ex) {
            throw ex;
        } catch (Exception ex) {
            eventPublisher.publishEvent(new LoginFailureEvent(
                loginRequest.cpfOrEmail(),
                "Erro inesperado: " + ex.getMessage(),
                requestInfo,
                ex
            ));
            throw ex;
        }
    }

    public LoginResponseRecord refreshToken(String refreshTokenString) {
        String requestInfo = HttpRequestHelper.formatRequestInfo(httpServletRequest);
        
        try {
            Claims claims = jwtService.parseToken(refreshTokenString);

            String cpf = claims.getSubject();
            User user = userService.findByCpf(cpf)
                .orElseThrow(() -> new BusinessRuleException(
                    HttpStatus.UNAUTHORIZED,
                    getClass(),
                    BusinessRules.USER_NOT_FOUND,
                    "Usuário não encontrado com o CPF: %s",
                    cpf
                ));
            
            RefreshToken refreshToken = refreshTokenService.fetchAValidRefreshToken(claims, user);
            refreshTokenService.saveAsUsedAndRevoked(refreshToken);

            String newJti = UUID.randomUUID().toString();
            String newAccessToken = jwtService.generateAccessToken(user, newJti);
            String newRefreshToken = jwtService.generateRefreshToken(user, newJti);

            refreshTokenService.saveNewRefreshToken(user, newJti);

            eventPublisher.publishEvent(new RefreshTokenEvent(
                user,
                cpf,
                requestInfo,
                true,
                "Refresh token utilizado com sucesso"
            ));

            return new LoginResponseRecord(newAccessToken, newRefreshToken, accessTokenExpiration);
        } catch (Exception ex) {
            String cpf = extractCpfFromToken(refreshTokenString);
            
            eventPublisher.publishEvent(new RefreshTokenEvent(
                null,
                cpf,
                requestInfo,
                false,
                "Falha ao utilizar refresh token: " + ex.getMessage()
            ));
            
            throw ex;
        }
    }

    private String extractCpfFromToken(String token) {
        try {
            Claims claims = jwtService.parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
