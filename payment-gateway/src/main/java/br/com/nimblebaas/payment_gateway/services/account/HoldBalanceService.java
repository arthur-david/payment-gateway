package br.com.nimblebaas.payment_gateway.services.account;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.account.HoldBalance;
import br.com.nimblebaas.payment_gateway.enums.account.HoldBalanceStatus;
import br.com.nimblebaas.payment_gateway.enums.account.HoldBalanceType;
import br.com.nimblebaas.payment_gateway.repositories.account.HoldBalanceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class HoldBalanceService {
    
    private final HoldBalanceRepository holdBalanceRepository;
    private final AccountService accountService;

    @Transactional
    public HoldBalance createHold(Account account, BigDecimal amount, HoldBalanceType type) {
        var newHoldBalance = account.getHoldBalance().add(amount); 
        account.setHoldBalance(newHoldBalance);
        accountService.save(account);

        var holdBalance = HoldBalance.builder()
            .account(account)
            .amount(amount)
            .type(type)
            .status(HoldBalanceStatus.PENDING)
            .build();
        holdBalanceRepository.save(holdBalance);

        return holdBalance;
    }

    @Transactional
    public void confirmHold(HoldBalance holdBalance) {
        accountService.makeWithdrawal(holdBalance.getAccount(), holdBalance.getAmount());

        var account = holdBalance.getAccount();
        var newHoldBalance = account.getHoldBalance().subtract(holdBalance.getAmount());
        account.setHoldBalance(newHoldBalance);
        accountService.save(account);

        holdBalance.setStatus(HoldBalanceStatus.CONFIRMED);
        holdBalanceRepository.save(holdBalance);
    }

    @Transactional
    public void cancelHold(HoldBalance holdBalance) {
        var account = holdBalance.getAccount();
        var newHoldBalance = account.getHoldBalance().subtract(holdBalance.getAmount());
        account.setHoldBalance(newHoldBalance);
        accountService.save(account);

        holdBalance.setStatus(HoldBalanceStatus.CANCELLED);
        holdBalanceRepository.save(holdBalance);
    }
}
