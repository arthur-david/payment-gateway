package br.com.nimblebaas.payment_gateway.dtos.output.user;

public record UserOutputRecord(
    String name,
    String cpf,
    String email
) {}
