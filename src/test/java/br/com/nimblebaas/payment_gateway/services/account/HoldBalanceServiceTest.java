package br.com.nimblebaas.payment_gateway.services.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.account.HoldBalance;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.account.HoldBalanceStatus;
import br.com.nimblebaas.payment_gateway.enums.account.HoldBalanceType;
import br.com.nimblebaas.payment_gateway.repositories.account.HoldBalanceRepository;

@ExtendWith(MockitoExtension.class)
class HoldBalanceServiceTest {

    @Mock
    private HoldBalanceRepository holdBalanceRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private HoldBalanceService holdBalanceService;

    private User user;
    private Account account;
    private HoldBalance holdBalance;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setCpf("12345678900");
        user.setEmail("john@example.com");

        account = new Account(user);
        account.setTotalBalance(new BigDecimal("1000.00"));
        account.setHoldBalance(BigDecimal.ZERO);

        holdBalance = HoldBalance.builder()
            .account(account)
            .amount(new BigDecimal("100.00"))
            .type(HoldBalanceType.CHARGE_PAYMENT)
            .status(HoldBalanceStatus.PENDING)
            .build();
    }

    @Test
    void createHold_ShouldCreateAndSaveHoldBalance() {
        BigDecimal holdAmount = new BigDecimal("200.00");

        when(accountService.save(any(Account.class))).thenReturn(account);
        when(holdBalanceRepository.save(any(HoldBalance.class))).thenReturn(holdBalance);

        HoldBalance result = holdBalanceService.createHold(account, holdAmount, HoldBalanceType.CHARGE_PAYMENT);

        assertNotNull(result);
        assertEquals(holdAmount, account.getHoldBalance());
        verify(accountService).save(account);
        verify(holdBalanceRepository).save(any(HoldBalance.class));
    }

    @Test
    void confirmHold_ShouldConfirmHoldAndUpdateBalance() {
        account.setHoldBalance(new BigDecimal("100.00"));
        account.setTotalBalance(new BigDecimal("1000.00"));

        when(accountService.save(any(Account.class))).thenReturn(account);
        when(holdBalanceRepository.save(any(HoldBalance.class))).thenReturn(holdBalance);

        holdBalanceService.confirmHold(holdBalance);

        assertEquals(0, account.getHoldBalance().compareTo(BigDecimal.ZERO));
        assertEquals(HoldBalanceStatus.CONFIRMED, holdBalance.getStatus());
        verify(accountService).makeWithdrawal(account, new BigDecimal("100.00"));
        verify(holdBalanceRepository).save(holdBalance);
    }

    @Test
    void cancelHold_ShouldCancelHoldAndReleaseBalance() {
        account.setHoldBalance(new BigDecimal("100.00"));

        when(accountService.save(any(Account.class))).thenReturn(account);
        when(holdBalanceRepository.save(any(HoldBalance.class))).thenReturn(holdBalance);

        holdBalanceService.cancelHold(holdBalance);

        assertEquals(0, account.getHoldBalance().compareTo(BigDecimal.ZERO));
        assertEquals(HoldBalanceStatus.CANCELLED, holdBalance.getStatus());
        verify(accountService).save(account);
        verify(holdBalanceRepository).save(holdBalance);
    }
}

