package br.com.nimblebaas.payment_gateway.services.account;

import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.repositories.account.AccountRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AccountService {
    
    private final AccountRepository accountRepository;

    public void openAccount(User user) {
        var account = new Account(user);
        accountRepository.save(account);
    }
}
