package br.com.nimblebaas.payment_gateway.controllers.charge;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.nimblebaas.payment_gateway.configs.authentication.UserAuthenticated;
import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargeCancelInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargeInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargePaymentInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.charge.ChargeOutputDTO;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;
import br.com.nimblebaas.payment_gateway.services.charge.ChargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/charges")
@Tag(name = "Cobranças", description = "Gerenciamento de cobranças, pagamentos e cancelamentos")
public class ChargeController {

    private final ChargeService chargeService;
    
    @PostMapping
    @Operation(
        summary = "Criar cobrança",
        description = "Cria uma nova cobrança para outro usuário usando seu CPF. " +
                      "A cobrança ficará pendente até ser paga ou cancelada.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Cobrança criada com sucesso",
            content = @Content(schema = @Schema(implementation = ChargeOutputDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou destinatário não encontrado"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ChargeOutputDTO> create(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @RequestBody ChargeInputRecord chargeInputRecord) {
        var charge = chargeService.create(userAuthenticated, chargeInputRecord);
        return ResponseEntity.status(HttpStatus.CREATED).body(charge);
    }

    @GetMapping("/sent")
    @Operation(
        summary = "Listar cobranças enviadas",
        description = "Retorna a lista de cobranças criadas pelo usuário autenticado. " +
                      "Pode filtrar por status (PENDING, PAID, CANCELLED).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de cobranças retornada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChargeOutputDTO.class)))
        ),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<ChargeOutputDTO>> getSentCharges(
            @AuthenticationPrincipal UserAuthenticated userAuthenticated,
            @Parameter(description = "Lista de status para filtrar (PENDING, PAID, CANCELLED)")
            @RequestParam(required = false) List<ChargeStatus> statuses) {
        var sentCharges = chargeService.getSentChargesByUser(userAuthenticated, statuses);
        return ResponseEntity.ok(sentCharges);
    }

    @GetMapping("/received")
    @Operation(
        summary = "Listar cobranças recebidas",
        description = "Retorna a lista de cobranças recebidas pelo usuário autenticado. " +
                      "Pode filtrar por status (PENDING, PAID, CANCELLED).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de cobranças retornada com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChargeOutputDTO.class)))
        ),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<ChargeOutputDTO>> getReceivedCharges(
            @AuthenticationPrincipal UserAuthenticated userAuthenticated,
            @Parameter(description = "Lista de status para filtrar (PENDING, PAID, CANCELLED)")
            @RequestParam(required = false) List<ChargeStatus> statuses) {
        var receivedCharges = chargeService.getReceivedChargesByUser(userAuthenticated, statuses);
        return ResponseEntity.ok(receivedCharges);
    }

    @PostMapping("/pay")
    @Operation(
        summary = "Pagar cobrança",
        description = "Realiza o pagamento de uma cobrança usando saldo ou cartão de crédito. " +
                      "Para pagamento com saldo, o valor é debitado da conta e creditado ao destinatário. " +
                      "Para pagamento com cartão, é feita validação com autorizador externo.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pagamento realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Cobrança inválida, saldo insuficiente ou pagamento não autorizado"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "404", description = "Cobrança não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> pay(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @RequestBody ChargePaymentInputRecord chargePaymentInputRecord) {
        chargeService.pay(userAuthenticated, chargePaymentInputRecord);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cancel")
    @Operation(
        summary = "Cancelar cobrança",
        description = "Cancela uma cobrança. Apenas o criador da cobrança pode cancelá-la. " +
                      "Cobranças pendentes são apenas marcadas como canceladas. " +
                      "Cobranças pagas com saldo são estornadas. " +
                      "Cobranças pagas com cartão consultam o autorizador externo para cancelamento.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cobrança cancelada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Cobrança inválida ou não pode ser cancelada"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "403", description = "Usuário não tem permissão para cancelar esta cobrança"),
        @ApiResponse(responseCode = "404", description = "Cobrança não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @RequestBody ChargeCancelInputRecord chargeCancelInputRecord) {
        chargeService.cancel(userAuthenticated, chargeCancelInputRecord);
        return ResponseEntity.noContent().build();
    }
}
