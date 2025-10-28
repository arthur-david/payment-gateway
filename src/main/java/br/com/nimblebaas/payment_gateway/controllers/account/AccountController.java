package br.com.nimblebaas.payment_gateway.controllers.account;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.nimblebaas.payment_gateway.configs.authentication.UserAuthenticated;
import br.com.nimblebaas.payment_gateway.dtos.input.account.MakeSelfDepositInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.account.BalanceOutputRecord;
import br.com.nimblebaas.payment_gateway.services.account.AccountService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/balance")
    public ResponseEntity<BalanceOutputRecord> getBalance(@AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        var balance = accountService.getBalance(userAuthenticated);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/deposit")
    public ResponseEntity<Void> makeSelfDeposit(
            @AuthenticationPrincipal UserAuthenticated userAuthenticated, 
            @RequestBody MakeSelfDepositInputRecord makeSelfDepositInputRecord) {
        accountService.makeSelfDeposit(makeSelfDepositInputRecord, userAuthenticated);
        return ResponseEntity.noContent().build();
    }
}
