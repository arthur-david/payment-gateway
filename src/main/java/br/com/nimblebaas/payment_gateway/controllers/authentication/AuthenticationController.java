package br.com.nimblebaas.payment_gateway.controllers.authentication;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.nimblebaas.payment_gateway.dtos.input.authentication.LoginRequestRecord;
import br.com.nimblebaas.payment_gateway.dtos.input.authentication.RefreshTokenRequestRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.authentication.LoginResponseRecord;
import br.com.nimblebaas.payment_gateway.services.authentication.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/authentication")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e gerenciamento de tokens")
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(
        summary = "Realizar login",
        description = "Autentica um usuário usando CPF ou e-mail e senha. " +
                      "Retorna um token de acesso (JWT) e um refresh token para renovação."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login realizado com sucesso",
            content = @Content(schema = @Schema(implementation = LoginResponseRecord.class))
        ),
        @ApiResponse(responseCode = "400", description = "Credenciais inválidas"),
        @ApiResponse(responseCode = "401", description = "CPF/e-mail ou senha incorretos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<LoginResponseRecord> login(@Valid @RequestBody LoginRequestRecord request) {
        LoginResponseRecord response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    @Operation(
        summary = "Renovar token de acesso",
        description = "Gera um novo token de acesso (JWT) e refresh token usando um refresh token válido. " +
                      "Use este endpoint quando o token de acesso expirar."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token renovado com sucesso",
            content = @Content(schema = @Schema(implementation = LoginResponseRecord.class))
        ),
        @ApiResponse(responseCode = "400", description = "Refresh token inválido"),
        @ApiResponse(responseCode = "401", description = "Refresh token expirado ou revogado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<LoginResponseRecord> refreshToken(@Valid @RequestBody RefreshTokenRequestRecord request) {
        LoginResponseRecord response = authenticationService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }
}
