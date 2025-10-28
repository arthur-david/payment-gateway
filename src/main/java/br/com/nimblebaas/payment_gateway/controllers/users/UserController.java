package br.com.nimblebaas.payment_gateway.controllers.users;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.nimblebaas.payment_gateway.configs.authentication.UserAuthenticated;
import br.com.nimblebaas.payment_gateway.dtos.input.user.ChangePasswordInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.input.user.UserInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.user.UserOutputRecord;
import br.com.nimblebaas.payment_gateway.services.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema")
public class UserController {

    private final UserService userService;

    @PostMapping("register")
    @Operation(
        summary = "Registrar novo usuário",
        description = "Cria um novo usuário no sistema com nome, CPF, e-mail e senha. " +
                      "O CPF deve ser válido e único. A senha será armazenada de forma segura."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuário criado com sucesso",
            content = @Content(schema = @Schema(implementation = UserOutputRecord.class))
        ),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF/e-mail já cadastrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<UserOutputRecord> create(@RequestBody UserInputRecord userInputRecord) {
        var userOutputRecord = userService.create(userInputRecord);

        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .queryParam("cpf", userOutputRecord.cpf())
            .build()
            .toUri();

        return ResponseEntity.created(uri).body(userOutputRecord);
    }

    @PatchMapping("password")
    @Operation(
        summary = "Alterar senha do usuário",
        description = "Permite que o usuário autenticado altere sua própria senha",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Senha atual incorreta ou nova senha inválida"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @RequestBody ChangePasswordInputRecord changePasswordInputRecord) {
        userService.changePassword(userAuthenticated, changePasswordInputRecord);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
        summary = "Buscar usuário por CPF ou e-mail",
        description = "Retorna os dados de um usuário a partir do CPF ou e-mail"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário encontrado",
            content = @Content(schema = @Schema(implementation = UserOutputRecord.class))
        ),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<UserOutputRecord> getUser(
            @Parameter(description = "CPF ou e-mail do usuário", required = true)
            @RequestParam(required = true) String cpfOrEmail) {
        var user = userService.getUser(cpfOrEmail);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    @Operation(
        summary = "Obter dados do usuário autenticado",
        description = "Retorna os dados do usuário atualmente autenticado",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dados do usuário retornados com sucesso",
            content = @Content(schema = @Schema(implementation = UserOutputRecord.class))
        ),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<UserOutputRecord> getCurrentUser(@AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        return ResponseEntity.ok(
            new UserOutputRecord(
                userAuthenticated.getUser().getName(),
                userAuthenticated.getUser().getCpf(),
                userAuthenticated.getUser().getEmail()
            )
        );
    }
}
