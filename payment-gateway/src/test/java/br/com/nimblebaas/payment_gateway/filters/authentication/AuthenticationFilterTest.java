package br.com.nimblebaas.payment_gateway.filters.authentication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.exceptions.ErrorDTO;
import br.com.nimblebaas.payment_gateway.services.authentication.JwtService;
import br.com.nimblebaas.payment_gateway.services.authentication.RefreshTokenService;
import br.com.nimblebaas.payment_gateway.services.user.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserService userService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthenticationFilter authenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private User user;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        user = new User();
        user.setCpf("12345678900");
        user.setName("Test User");
        user.setEmail("test@email.com");
    }

    @Test
    void shouldAllowPublicEndpoint() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/users/register");

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).parseToken(anyString());
    }

    @Test
    void shouldAuthenticateSuccessfully() throws ServletException, IOException {
        String token = "valid.jwt.token";
        
        when(request.getRequestURI()).thenReturn("/api/v1/accounts/balance");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("12345678900");
        when(userService.findByCpf("12345678900")).thenReturn(Optional.of(user));

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService, times(1)).parseToken(token);
        verify(userService, times(1)).findByCpf("12345678900");
        verify(refreshTokenService, times(1)).validateAccessToken(claims, user);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldFailWhenTokenNotProvided() throws ServletException, IOException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        
        when(request.getRequestURI()).thenReturn("/api/v1/accounts/balance");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);
        when(objectMapper.writeValueAsString(any(ErrorDTO.class))).thenReturn("{\"status\":\"UNAUTHORIZED\"}");

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response, times(1)).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldFailWhenUserNotFound() throws ServletException, IOException {
        String token = "valid.jwt.token";
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        
        when(request.getRequestURI()).thenReturn("/api/v1/accounts/balance");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("12345678900");
        when(userService.findByCpf("12345678900")).thenReturn(Optional.empty());
        when(response.getWriter()).thenReturn(writer);
        when(objectMapper.writeValueAsString(any(ErrorDTO.class))).thenReturn("{\"status\":\"UNAUTHORIZED\"}");

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response, times(1)).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldHandleGenericException() throws ServletException, IOException {
        String token = "valid.jwt.token";
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        
        when(request.getRequestURI()).thenReturn("/api/v1/accounts/balance");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.parseToken(token)).thenThrow(new RuntimeException("Unexpected error"));
        when(response.getWriter()).thenReturn(writer);
        when(objectMapper.writeValueAsString(any(ErrorDTO.class))).thenReturn("{\"status\":\"INTERNAL_SERVER_ERROR\"}");

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response, times(1)).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldExtractTokenCorrectly() throws ServletException, IOException {
        String token = "my.jwt.token";
        
        when(request.getRequestURI()).thenReturn("/api/v1/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.parseToken(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("12345678900");
        when(userService.findByCpf("12345678900")).thenReturn(Optional.of(user));

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService, times(1)).parseToken(token);
        verify(filterChain, times(1)).doFilter(request, response);
    }
}

