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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
@Tag(name = "Contas", description = "Gerenciamento de contas e saldo dos usuários")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/balance")
    @Operation(
        summary = "Consultar saldo",
        description = "Retorna o saldo atual da conta do usuário autenticado",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Saldo retornado com sucesso",
            content = @Content(schema = @Schema(implementation = BalanceOutputRecord.class))
        ),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<BalanceOutputRecord> getBalance(@AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        var balance = accountService.getBalance(userAuthenticated);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/deposit")
    @Operation(
        summary = "Realizar depósito",
        description = "Adiciona saldo à conta do usuário autenticado através de cartão de crédito. " +
                      "O depósito é validado pelo autorizador externo antes de ser confirmado.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Depósito realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados do cartão inválidos ou depósito não autorizado"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> makeSelfDeposit(
            @AuthenticationPrincipal UserAuthenticated userAuthenticated, 
            @RequestBody MakeSelfDepositInputRecord makeSelfDepositInputRecord) {
        accountService.makeSelfDeposit(makeSelfDepositInputRecord, userAuthenticated);
        return ResponseEntity.noContent().build();
    }
}
