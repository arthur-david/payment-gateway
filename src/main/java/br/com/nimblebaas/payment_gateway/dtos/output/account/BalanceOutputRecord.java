package br.com.nimblebaas.payment_gateway.dtos.output.account;

import java.math.BigDecimal;

import br.com.nimblebaas.payment_gateway.entities.account.Account;

public record BalanceOutputRecord(
    BigDecimal totalBalance,
    BigDecimal holdBalance,
    BigDecimal availableBalance
) {

    public BalanceOutputRecord(Account account) {
        this(account.getTotalBalance(), account.getHoldBalance(), account.getAvailableBalance());
    }
}
