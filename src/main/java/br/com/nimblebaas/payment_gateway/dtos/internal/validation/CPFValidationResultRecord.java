package br.com.nimblebaas.payment_gateway.dtos.internal.validation;

public record CPFValidationResultRecord(

    boolean isValid,
    String message
) {}
