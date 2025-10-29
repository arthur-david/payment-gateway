package br.com.nimblebaas.payment_gateway.configs.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.nimblebaas.payment_gateway.filters.authentication.AuthenticationFilter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final AuthenticationFilter authenticationFilter;
    
    private static final String[] PUBLIC_ENDPOINTS = {
        "/users/register",
        "/authentication/login",
        "/authentication/refresh-token",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/api-docs/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpsSecurity) throws Exception {
        httpsSecurity
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return httpsSecurity.build();
    }

    public static String[] getPublicEndpoints() {
        return PUBLIC_ENDPOINTS;
    }
}
