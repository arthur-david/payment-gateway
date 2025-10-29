package br.com.nimblebaas.payment_gateway.clients.authorizer.clients;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.nimblebaas.payment_gateway.clients.authorizer.models.AuthorizerResponse;
import br.com.nimblebaas.payment_gateway.clients.configuration.FeignClientConfig;

@FeignClient(value = "authorizer-api", url = "${app.api.authorizer.url}", configuration = FeignClientConfig.class)
public interface AuthorizerApiClient {
    
    @GetMapping("authorizer")
    AuthorizerResponse authorizeCardPayment(
        @RequestParam(name = "cpf") String cpf,
        @RequestParam(name = "amount") BigDecimal amount,
        @RequestParam(name = "installments") Integer installments,
        @RequestParam(name = "cardNumber") String cardNumber,
        @RequestParam(name = "cardCvv") String cardCvv,
        @RequestParam(name = "cardExpirationDate") String cardExpirationDate,
        @RequestParam(name = "identifier") String identifier);

    @GetMapping("authorizer")
    AuthorizerResponse authorizeDeposit(
        @RequestParam(name = "cpf") String cpf,
        @RequestParam(name = "amount") BigDecimal amount,
        @RequestParam(name = "identifier") String identifier);

    @GetMapping("authorizer")
    AuthorizerResponse authorizeCardRefund(
        @RequestParam(name = "cpf") String cpf,
        @RequestParam(name = "amount") BigDecimal amount,
        @RequestParam(name = "identifier") String identifier);
}
