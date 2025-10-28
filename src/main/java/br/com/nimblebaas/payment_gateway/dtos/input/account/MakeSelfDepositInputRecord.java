package br.com.nimblebaas.payment_gateway.dtos.input.account;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record MakeSelfDepositInputRecord(
    
    @NotNull(message = "O valor do depósito é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor do depósito deve ser maior que 0")
    BigDecimal amount
) {}
