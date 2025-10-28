package br.com.nimblebaas.payment_gateway.filters.authentication;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.nimblebaas.payment_gateway.configs.authentication.UserAuthenticated;
import br.com.nimblebaas.payment_gateway.configs.security.SecurityConfig;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.exceptions.ErrorDTO;
import br.com.nimblebaas.payment_gateway.services.authentication.JwtService;
import br.com.nimblebaas.payment_gateway.services.authentication.RefreshTokenService;
import br.com.nimblebaas.payment_gateway.services.user.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, 
            @NonNull HttpServletResponse response, 
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractToken(request);

            Claims claims = jwtService.parseToken(token);

            String cpf = claims.getSubject();
            User user = getUser(cpf);

            refreshTokenService.validateAccessToken(claims, user);

            UserAuthenticated userAuthenticated = new UserAuthenticated(user);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userAuthenticated,
                null,
                userAuthenticated.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (BusinessRuleException ex) {
            handleBusinessRuleException(response, ex);
        } catch (Exception ex) {
            handleGenericException(response, ex);
        }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return Arrays.stream(SecurityConfig.getPublicEndpoints())
            .anyMatch(endpoint -> pathMatcher.match(endpoint, requestURI));
    }

    private String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null)
            return authorizationHeader.replace("Bearer ", "");
        
        throw new BusinessRuleException(HttpStatus.UNAUTHORIZED, getClass(), BusinessRules.TOKEN_REQUIRED, "Token não encontrado");
    }

    private User getUser(String cpf) {
        return userService.findByCpf(cpf)
            .orElseThrow(() -> new BusinessRuleException(HttpStatus.UNAUTHORIZED, getClass(), BusinessRules.USER_NOT_FOUND, "Usuário não encontrado com o CPF: %s", cpf));
    }

    private void handleBusinessRuleException(HttpServletResponse response, BusinessRuleException ex) throws IOException {
        log.error("BusinessRuleException no filtro de autenticação: {} - Source: {}", ex.getMessage(), ex.getSource(), ex);
        
        ErrorDTO errorDTO = ex.getErrorDTO();
        response.setStatus(errorDTO.getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorDTO));
    }

    private void handleGenericException(HttpServletResponse response, Exception ex) throws IOException {
        log.error("Erro inesperado no filtro de autenticação: {}", ex.getMessage(), ex);
        
        ErrorDTO errorDTO = ErrorDTO.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .reason("INTERNAL_ERROR")
            .details("Erro ao processar autenticação")
            .build();
        
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorDTO));
    }
}
