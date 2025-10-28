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
import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargeInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.charge.ChargeOutputDTO;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;
import br.com.nimblebaas.payment_gateway.services.charge.ChargeService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/charges")
public class ChargeController {

    private final ChargeService chargeService;
    
    @PostMapping
    public ResponseEntity<ChargeOutputDTO> create(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @RequestBody ChargeInputRecord chargeInputRecord) {
        var charge = chargeService.create(userAuthenticated, chargeInputRecord);
        return ResponseEntity.status(HttpStatus.CREATED).body(charge);
    }

    @GetMapping("/sent")
    public ResponseEntity<List<ChargeOutputDTO>> getSentCharges(
            @AuthenticationPrincipal UserAuthenticated userAuthenticated,
            @RequestParam(required = false) List<ChargeStatus> statuses) {
        var sentCharges = chargeService.getSentChargesByUser(userAuthenticated, statuses);
        return ResponseEntity.ok(sentCharges);
    }

    @GetMapping("/received")
    public ResponseEntity<List<ChargeOutputDTO>> getReceivedCharges(
            @AuthenticationPrincipal UserAuthenticated userAuthenticated,
            @RequestParam(required = false) List<ChargeStatus> statuses) {
        var receivedCharges = chargeService.getReceivedChargesByUser(userAuthenticated, statuses);
        return ResponseEntity.ok(receivedCharges);
    }
}
