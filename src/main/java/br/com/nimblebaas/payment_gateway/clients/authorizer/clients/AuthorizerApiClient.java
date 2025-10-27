package br.com.nimblebaas.payment_gateway.clients.authorizer.clients;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.nimblebaas.payment_gateway.clients.authorizer.models.AuthorizerResponse;
import br.com.nimblebaas.payment_gateway.clients.configuration.FeignClientConfig;

@FeignClient(value = "authorizer-api", url = "${app.api.authorizer.url}", configuration = FeignClientConfig.class)
public interface AuthorizerApiClient {
    
    @PostMapping("authorize")
    ResponseEntity<AuthorizerResponse> authorizeCardPayment(
        @RequestParam(name = "cpf") String cpf,
        @RequestParam(name = "cardNumber") String cardNumber,
        @RequestParam(name = "cvv") String cvv,
        @RequestParam(name = "amount") BigDecimal amount,
        @RequestParam(name = "installments") Integer installments,
        @RequestParam(name = "identifier") String identifier);

    @PostMapping("authorize")
    ResponseEntity<AuthorizerResponse> authorizeDeposit(
        @RequestParam(name = "cpf") String cpf,
        @RequestParam(name = "amount") BigDecimal amount,
        @RequestParam(name = "identifier") String identifier);

    @PostMapping("authorize")
    ResponseEntity<AuthorizerResponse> authorizeCardRefund(
        @RequestParam(name = "cpf") String cpf,
        @RequestParam(name = "identifier") String identifier);
}
