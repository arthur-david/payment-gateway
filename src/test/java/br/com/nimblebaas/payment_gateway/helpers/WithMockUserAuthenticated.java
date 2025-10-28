package br.com.nimblebaas.payment_gateway.helpers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUserAuthenticatedSecurityContextFactory.class)
public @interface WithMockUserAuthenticated {
    
    String name() default "Test User";
    
    String cpf() default "12345678900";
    
    String email() default "test@example.com";
    
    String password() default "password123";
}

