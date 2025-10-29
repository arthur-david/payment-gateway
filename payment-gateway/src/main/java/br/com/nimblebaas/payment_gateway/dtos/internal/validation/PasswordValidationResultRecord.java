package br.com.nimblebaas.payment_gateway.dtos.internal.validation;

public record PasswordValidationResultRecord(
    boolean isStrong,
    String message
) {}
