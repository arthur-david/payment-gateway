package br.com.nimblebaas.payment_gateway.dtos.output.account;

import java.math.BigDecimal;

import br.com.nimblebaas.payment_gateway.entities.account.Account;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informações de saldo da conta")
public record BalanceOutputRecord(
    @Schema(description = "Saldo total da conta em reais", example = "1000.00")
    BigDecimal totalBalance,
    
    @Schema(description = "Saldo bloqueado/em espera em reais", example = "50.00")
    BigDecimal holdBalance,
    
    @Schema(description = "Saldo disponível para uso em reais", example = "950.00")
    BigDecimal availableBalance
) {

    public BalanceOutputRecord(Account account) {
        this(account.getTotalBalance(), account.getHoldBalance(), account.getAvailableBalance());
    }
}
