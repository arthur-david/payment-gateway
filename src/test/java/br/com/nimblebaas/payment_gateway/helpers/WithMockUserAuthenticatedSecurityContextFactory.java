package br.com.nimblebaas.payment_gateway.helpers;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import br.com.nimblebaas.payment_gateway.configs.authentication.UserAuthenticated;
import br.com.nimblebaas.payment_gateway.entities.user.User;

public class WithMockUserAuthenticatedSecurityContextFactory 
    implements WithSecurityContextFactory<WithMockUserAuthenticated> {

    @Override
    public SecurityContext createSecurityContext(WithMockUserAuthenticated annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User user = new User();
        user.setName(annotation.name());
        user.setCpf(annotation.cpf());
        user.setEmail(annotation.email());
        user.setPassword(annotation.password());

        UserAuthenticated principal = new UserAuthenticated(user);
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        
        context.setAuthentication(authentication);
        
        return context;
    }
}

