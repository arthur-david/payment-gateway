package br.com.nimblebaas.payment_gateway.controllers.user;

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
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping("register")
    public ResponseEntity<UserOutputRecord> create(@RequestBody UserInputRecord userInputRecord) {
        var userOutputRecord = userService.create(userInputRecord);

        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .queryParam("cpf", userOutputRecord.cpf())
            .build()
            .toUri();

        return ResponseEntity.created(uri).body(userOutputRecord);
    }

    @PatchMapping("password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @RequestBody ChangePasswordInputRecord changePasswordInputRecord) {
        userService.changePassword(userAuthenticated, changePasswordInputRecord);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<UserOutputRecord> getUser(
            @RequestParam(required = true) String cpfOrEmail) {
        var user = userService.getUser(cpfOrEmail);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
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
