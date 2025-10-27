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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/authentication")
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseRecord> login(@Valid @RequestBody LoginRequestRecord request) {
        LoginResponseRecord response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseRecord> refreshToken(@Valid @RequestBody RefreshTokenRequestRecord request) {
        LoginResponseRecord response = authenticationService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }
}
