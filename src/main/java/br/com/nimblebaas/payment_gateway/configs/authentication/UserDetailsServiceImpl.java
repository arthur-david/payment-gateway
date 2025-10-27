package br.com.nimblebaas.payment_gateway.configs.authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.services.user.UserService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.findByCpfOrEmail(username)
            .map(UserAuthenticated::new)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }   
}
