package br.com.nimblebaas.payment_gateway.services.charge.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.dtos.internal.charge.ChargePaymentDTO;
import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.entities.charge.ChargePayment;
import br.com.nimblebaas.payment_gateway.enums.account.HoldBalanceType;
import br.com.nimblebaas.payment_gateway.enums.charge.PaymentMethod;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.repositories.charge.ChargePaymentRepository;
import br.com.nimblebaas.payment_gateway.services.account.AccountService;
import br.com.nimblebaas.payment_gateway.services.account.HoldBalanceService;
import br.com.nimblebaas.payment_gateway.services.transaction.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BalanceChargePaymentService implements IChargePaymentService {

    private final HoldBalanceService holdBalanceService;
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final ChargePaymentRepository chargePaymentRepository;
    
    @Override
    public boolean isResponsible(PaymentMethod paymentMethod) {
        return paymentMethod.equals(PaymentMethod.ACCOUNT_BALANCE);
    }

    @Transactional
    @Override
    public void pay(ChargePaymentDTO chargePaymentDTO) {
        var charge = chargePaymentDTO.getCharge();
        var destinationAccount = charge.getDestinationUser().getAccount();

        verifyDestinationAccountBalance(destinationAccount, charge.getAmount());

        var holdBalance = holdBalanceService.createHold(destinationAccount, charge.getAmount(), HoldBalanceType.CHARGE_PAYMENT);
        var debitTransaction = transactionService.createChargePaymentDebitTransaction(charge, holdBalance);

        try{
            makeDeposit(charge, charge.getAmount());
        } catch (BusinessRuleException e) {
            holdBalanceService.cancelHold(holdBalance);
            transactionService.completeFailedTransaction(debitTransaction, e.getMessage());
            throw e;
        }

        holdBalanceService.confirmHold(holdBalance);
        transactionService.completeSuccessTransaction(debitTransaction);

        saveChargePayment(charge);
    }

    private void verifyDestinationAccountBalance(Account destinationAccount, BigDecimal amount) {
        if (destinationAccount.getAvailableBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException(
                getClass(), 
                BusinessRules.INSUFFICIENT_BALANCE, 
                "Saldo insuficiente para pagar a cobrança");
        }
    }

    private void makeDeposit(Charge charge, BigDecimal amount) {
        var creditTransaction = transactionService.createChargePaymentCreditTransaction(
            charge,
            null
        );

        try{
            accountService.makeDeposit(charge.getOriginatorUser().getAccount(), amount);
        } catch (BusinessRuleException e) {
            transactionService.completeFailedTransaction(creditTransaction, e.getMessage());
            throw e;
        }

        transactionService.completeSuccessTransaction(creditTransaction);
    }

    private void saveChargePayment(Charge charge) {
        var chargePayment = ChargePayment.builder()
            .charge(charge)
            .paymentMethod(PaymentMethod.ACCOUNT_BALANCE)
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
