package br.com.nimblebaas.payment_gateway.services.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.nimblebaas.payment_gateway.configs.authentication.UserAuthenticated;
import br.com.nimblebaas.payment_gateway.dtos.input.account.MakeSelfDepositInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.GetAuthorizerDTO;
import br.com.nimblebaas.payment_gateway.dtos.output.account.BalanceOutputRecord;
import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.transaction.Transaction;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.authorizer.AuthorizerPurpose;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.repositories.account.AccountRepository;
import br.com.nimblebaas.payment_gateway.services.authorizer.AuthorizerService;
import br.com.nimblebaas.payment_gateway.services.transaction.TransactionService;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuthorizerService authorizerService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AccountService accountService;

    private User user;
    private Account account;
    private UserAuthenticated userAuthenticated;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setCpf("12345678900");
        user.setEmail("john@example.com");

        account = new Account(user);
        account.setTotalBalance(new BigDecimal("1000.00"));
        account.setHoldBalance(BigDecimal.ZERO);

        userAuthenticated = new UserAuthenticated(user);
    }

    @Test
    void openAccount_ShouldCreateAndSaveAccount() {
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.openAccount(user);

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void getBalance_WithValidUser_ShouldReturnBalance() {
        when(accountRepository.findByUser(any(User.class))).thenReturn(Optional.of(account));

        BalanceOutputRecord result = accountService.getBalance(userAuthenticated);

        assertNotNull(result);
        assertEquals(new BigDecimal("1000.00"), result.totalBalance());
        assertEquals(BigDecimal.ZERO, result.holdBalance());
        assertEquals(new BigDecimal("1000.00"), result.availableBalance());
    }

    @Test
    void getBalance_WithInvalidUser_ShouldThrowException() {
        when(accountRepository.findByUser(any(User.class))).thenReturn(Optional.empty());

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> accountService.getBalance(userAuthenticated)
        );

        assertEquals(BusinessRules.ACCOUNT_NOT_FOUND.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void makeSelfDeposit_WithAuthorization_ShouldDepositSuccessfully() {
        MakeSelfDepositInputRecord depositInput = new MakeSelfDepositInputRecord(new BigDecimal("100.00"));
        Transaction transaction = new Transaction();

        when(accountRepository.findByUser(any(User.class))).thenReturn(Optional.of(account));
        when(transactionService.createDepositTransaction(any(), any(), anyString())).thenReturn(transaction);
        when(authorizerService.authorize(any(AuthorizerPurpose.class), any(GetAuthorizerDTO.class))).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.makeSelfDeposit(depositInput, userAuthenticated);

        verify(transactionService).completeSuccessTransaction(transaction);
        verify(accountRepository).save(account);
    }

    @Test
    void makeSelfDeposit_WithoutAuthorization_ShouldThrowException() {
        MakeSelfDepositInputRecord depositInput = new MakeSelfDepositInputRecord(new BigDecimal("100.00"));
        Transaction transaction = new Transaction();

        when(accountRepository.findByUser(any(User.class))).thenReturn(Optional.of(account));
        when(transactionService.createDepositTransaction(any(), any(), anyString())).thenReturn(transaction);
        when(authorizerService.authorize(any(AuthorizerPurpose.class), any(GetAuthorizerDTO.class))).thenReturn(false);

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> accountService.makeSelfDeposit(depositInput, userAuthenticated)
        );

        assertEquals(BusinessRules.AUTHORIZATION_FAILED.name(), exception.getErrorDTO().getReason());
        verify(transactionService).completeFailedTransaction(transaction, "Depósito não autorizado");
    }

    @Test
    void makeWithdrawal_WithSufficientBalance_ShouldWithdrawSuccessfully() {
        BigDecimal withdrawalAmount = new BigDecimal("500.00");

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.makeWithdrawal(account, withdrawalAmount);

        assertEquals(new BigDecimal("500.00"), account.getTotalBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void makeWithdrawal_WithInsufficientBalance_ShouldThrowException() {
        BigDecimal withdrawalAmount = new BigDecimal("1500.00");

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> accountService.makeWithdrawal(account, withdrawalAmount)
        );

        assertEquals(BusinessRules.INSUFFICIENT_BALANCE.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void makeWithdrawal_WithNegativeAmount_ShouldThrowException() {
        BigDecimal withdrawalAmount = new BigDecimal("-100.00");

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> accountService.makeWithdrawal(account, withdrawalAmount)
        );

        assertEquals(BusinessRules.INVALID_AMOUNT_TO_WITHDRAWAL.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void makeDeposit_WithPositiveAmount_ShouldDepositSuccessfully() {
        BigDecimal depositAmount = new BigDecimal("500.00");

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.makeDeposit(account, depositAmount);

        assertEquals(new BigDecimal("1500.00"), account.getTotalBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void makeDeposit_WithNegativeAmount_ShouldThrowException() {
        BigDecimal depositAmount = new BigDecimal("-100.00");

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> accountService.makeDeposit(account, depositAmount)
        );

        assertEquals(BusinessRules.INVALID_AMOUNT_TO_DEPOSIT.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void makeDeposit_WithZeroAmount_ShouldThrowException() {
        BigDecimal depositAmount = BigDecimal.ZERO;

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> accountService.makeDeposit(account, depositAmount)
        );

        assertEquals(BusinessRules.INVALID_AMOUNT_TO_DEPOSIT.name(), exception.getErrorDTO().getReason());
    }
}

