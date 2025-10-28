package br.com.nimblebaas.payment_gateway.services.charge.payment;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.dtos.internal.charge.ChargePaymentDTO;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.enums.charge.PaymentMethod;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChargePaymentService {

    private final List<IChargePaymentService> chargePaymentServices;

    private IChargePaymentService getChargePaymentService(PaymentMethod paymentMethod) {
        return chargePaymentServices.stream()
            .filter(service -> service.isResponsible(paymentMethod))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleException(
                getClass(), 
                BusinessRules.CHARGE_PAYMENT_SERVICE_NOT_FOUND, 
                "No charge payment service found for payment method: %s", paymentMethod.name()));
    }

    public void pay(@Valid ChargePaymentDTO chargePaymentDTO, PaymentMethod paymentMethod) {
        getChargePaymentService(paymentMethod).pay(chargePaymentDTO);
    }

    public void cancel(Charge charge) {
        getChargePaymentService(charge.getPayment().getPaymentMethod()).cancel(charge);
    }
}
