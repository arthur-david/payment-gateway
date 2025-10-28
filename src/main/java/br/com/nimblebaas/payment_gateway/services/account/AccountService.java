package br.com.nimblebaas.payment_gateway.services.account;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.configs.authentication.UserAuthenticated;
import br.com.nimblebaas.payment_gateway.dtos.input.account.MakeSelfDepositInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.GetAuthorizerDTO;
import br.com.nimblebaas.payment_gateway.dtos.output.account.BalanceOutputRecord;
import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.authorizer.AuthorizerPurpose;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.repositories.account.AccountRepository;
import br.com.nimblebaas.payment_gateway.services.authorizer.factory.AuthorizerServiceFactory;
import br.com.nimblebaas.payment_gateway.services.transaction.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final AuthorizerServiceFactory authorizerServiceFactory;
    private final TransactionService transactionService;

    public void openAccount(User user) {
        var account = new Account(user);
        accountRepository.save(account);
    }

    public BalanceOutputRecord getBalance(UserAuthenticated userAuthenticated) {
        var account = accountRepository.findByUser(userAuthenticated.getUser())
            .orElseThrow(() -> new BusinessRuleException(
                getClass(), 
                BusinessRules.ACCOUNT_NOT_FOUND, 
                "Conta não encontrada"));
        return new BalanceOutputRecord(account);
    }

    public void makeSelfDeposit(@Valid MakeSelfDepositInputRecord makeSelfDepositInputRecord, UserAuthenticated userAuthenticated) {
        var account = accountRepository.findByUser(userAuthenticated.getUser())
            .orElseThrow(() -> new BusinessRuleException(
                getClass(), 
                BusinessRules.ACCOUNT_NOT_FOUND, 
                "Conta não encontrada"));

        var identifier = String.format("%s_%s", AuthorizerPurpose.DEPOSIT.name(), UUID.randomUUID());

        var transaction = transactionService.createDepositTransaction(account, makeSelfDepositInputRecord.amount(), identifier);

        var authorized = authorizeDeposit(userAuthenticated.getUser().getCpf(), identifier, makeSelfDepositInputRecord.amount());

        if (!authorized) {
            transactionService.completeFailedTransaction(transaction, "Depósito não autorizado");
            throw new BusinessRuleException(
                getClass(), 
                BusinessRules.AUTHORIZATION_FAILED, 
                "Depósito não autorizado");
        }

        makeDeposit(account, makeSelfDepositInputRecord.amount());
        
        transactionService.completeSuccessTransaction(transaction);
    }

    public void makeWithdraw(Account account, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException(
                getClass(), 
                BusinessRules.INVALID_AMOUNT_TO_WITHDRAW, 
                "O valor do saque deve ser maior que 0");
        }

        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException(
                getClass(), 
                BusinessRules.INSUFFICIENT_BALANCE, 
                "Saldo insuficiente");
        }

        account.setTotalBalance(account.getTotalBalance().subtract(amount));
        accountRepository.save(account);
    }

    public void makeDeposit(Account account, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException(
                getClass(), 
                BusinessRules.INVALID_AMOUNT_TO_DEPOSIT, 
                "O valor do depósito deve ser maior que 0");
        }

        account.setTotalBalance(account.getTotalBalance().add(amount));
        accountRepository.save(account);
    }

    private boolean authorizeDeposit(String cpf, String identifier, BigDecimal amount) {
        var authorizerService = authorizerServiceFactory.getAuthorizerService(AuthorizerPurpose.DEPOSIT);
        var getAuthorizerDTO = GetAuthorizerDTO.builder()
            .cpf(cpf)
            .identifier(identifier)
            .amount(amount)
            .build();
        return authorizerService.authorize(getAuthorizerDTO);
    }
}
