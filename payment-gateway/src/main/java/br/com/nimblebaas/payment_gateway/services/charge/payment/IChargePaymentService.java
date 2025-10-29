package br.com.nimblebaas.payment_gateway.services.charge.payment;

import br.com.nimblebaas.payment_gateway.dtos.internal.charge.ChargePaymentDTO;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.enums.charge.PaymentMethod;

public interface IChargePaymentService {
    
    boolean isResponsible(PaymentMethod paymentMethod);
    void pay(ChargePaymentDTO chargePaymentDTO);
    void cancel(Charge charge);
}