package br.com.nimblebaas.payment_gateway.services.charge.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.AuthorizerCardDetailsDTO;
import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.GetAuthorizerDTO;
import br.com.nimblebaas.payment_gateway.dtos.internal.charge.ChargePaymentDTO;
import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.entities.charge.ChargePayment;
import br.com.nimblebaas.payment_gateway.enums.account.HoldBalanceType;
import br.com.nimblebaas.payment_gateway.enums.authorizer.AuthorizerPurpose;
import br.com.nimblebaas.payment_gateway.enums.charge.PaymentMethod;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.repositories.charge.ChargePaymentRepository;
import br.com.nimblebaas.payment_gateway.services.account.AccountService;
import br.com.nimblebaas.payment_gateway.services.account.HoldBalanceService;
import br.com.nimblebaas.payment_gateway.services.authorizer.AuthorizerService;
import br.com.nimblebaas.payment_gateway.services.transaction.TransactionService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CardChargePaymentService implements IChargePaymentService {

    private final TransactionService transactionService;
    private final AuthorizerService authorizerService;
    private final AccountService accountService;
    private final HoldBalanceService holdBalanceService;
    private final ChargePaymentRepository chargePaymentRepository;
    
    @Override
    public boolean isResponsible(PaymentMethod paymentMethod) {
        return paymentMethod.equals(PaymentMethod.CREDIT_CARD);
    }

    @Override
    public void pay(ChargePaymentDTO chargePaymentDTO) {
        var charge = chargePaymentDTO.getCharge();

        var authorizationIdentifier = String.format("%s_%s", AuthorizerPurpose.CARD_PAYMENT.name(), charge.getIdentifier());

        var authorized = authorizeChargePayment(charge.getDestinationUser().getCpf(), authorizationIdentifier, charge.getAmount(), chargePaymentDTO);

        if (!authorized) {            
            throw new BusinessRuleException(
                getClass(), 
                BusinessRules.AUTHORIZATION_FAILED, 
                "Cobrança não autorizada");
        }

        makeDeposit(charge, charge.getAmount(), authorizationIdentifier);

        saveChargePayment(charge, authorizationIdentifier, chargePaymentDTO.getCardNumber());
    }

    private boolean authorizeChargePayment(String cpf, String authorizationIdentifier, BigDecimal amount, ChargePaymentDTO chargePaymentDTO) {
        var getAuthorizerDTO = GetAuthorizerDTO.builder()
            .cpf(cpf)
            .amount(amount)
            .identifier(authorizationIdentifier)
            .cardDetails(AuthorizerCardDetailsDTO.builder()
                .cardNumber(chargePaymentDTO.getCardNumber())
                .cardExpirationDate(chargePaymentDTO.getCardExpirationDate())
                .cardCvv(chargePaymentDTO.getCardCvv())
                .installments(chargePaymentDTO.getInstallments())
                .build())
            .build();
        return authorizerService.authorize(AuthorizerPurpose.CARD_PAYMENT, getAuthorizerDTO);
    }

    private void makeDeposit(Charge charge, BigDecimal amount, String authorizationIdentifier) {
        var creditTransaction = transactionService.createChargePaymentCreditTransaction(
            charge,
            authorizationIdentifier
        );

        try{
            accountService.makeDeposit(charge.getOriginatorUser().getAccount(), amount);
        } catch (BusinessRuleException e) {
            transactionService.completeFailedTransaction(creditTransaction, e.getMessage());
            throw e;
        }

        transactionService.completeSuccessTransaction(creditTransaction);
    }

    private void saveChargePayment(Charge charge, String authorizationIdentifier, String cardNumber) {
        var chargePayment = ChargePayment.builder()
            .charge(charge)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .authorizationIdentifier(authorizationIdentifier)
            .cardNumber(cardNumber)
            .paidAt(LocalDateTime.now())
            .build();
        chargePaymentRepository.save(chargePayment);
    }

    @Override
    public void cancel(Charge charge) {
        var originatorAccount = charge.getOriginatorUser().getAccount();

        verifyOriginatorAccountBalance(originatorAccount, charge.getAmount());

        var holdBalance = holdBalanceService.createHold(originatorAccount, charge.getAmount(), HoldBalanceType.CHARGE_REFUND);
        var debitTransaction = transactionService.createChargeRefundDebitTransaction(charge, holdBalance);

        var authorized = authorizeChargeRefund(charge.getOriginatorUser().getCpf(), charge.getPayment().getAuthorizationIdentifier(), charge.getAmount());

        if (!authorized) {            
            throw new BusinessRuleException(
                getClass(), 
                BusinessRules.AUTHORIZATION_FAILED, 
                "Reembolso não autorizado");
        }

        try{
            makeRefund(charge);
        } catch (BusinessRuleException e) {
            holdBalanceService.cancelHold(holdBalance);
            transactionService.completeFailedTransaction(debitTransaction, e.getMessage());
            throw e;
        }

        holdBalanceService.confirmHold(holdBalance);
        transactionService.completeSuccessTransaction(debitTransaction);

        saveChargeRefund(charge.getPayment());
    }

    private boolean authorizeChargeRefund(String cpf, String authorizationIdentifier, BigDecimal amount) {
        var getAuthorizerDTO = GetAuthorizerDTO.builder()
            .cpf(cpf)
            .amount(amount)
            .identifier(authorizationIdentifier)
            .build();
        return authorizerService.authorize(AuthorizerPurpose.CARD_REFUND, getAuthorizerDTO);
    }

    private void makeRefund(Charge charge) {
        var creditTransaction = transactionService.createChargeRefundCreditTransaction(charge);

        try{
            accountService.makeDeposit(charge.getDestinationUser().getAccount(), charge.getAmount());
        } catch (BusinessRuleException e) {
            transactionService.completeFailedTransaction(creditTransaction, e.getMessage());
            throw e;
        }
    }

    private void saveChargeRefund(ChargePayment chargePayment) {
        chargePayment.setCancelledAt(LocalDateTime.now());
        chargePaymentRepository.save(chargePayment);
    }

    private void verifyOriginatorAccountBalance(Account originatorAccount, BigDecimal amount) {
        if (originatorAccount.getAvailableBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException(
                getClass(), 
                BusinessRules.INSUFFICIENT_BALANCE, 
                "Saldo insuficiente para reembolsar a cobrança");
        }
    }
}
