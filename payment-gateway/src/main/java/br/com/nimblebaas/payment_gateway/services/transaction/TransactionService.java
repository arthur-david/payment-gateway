package br.com.nimblebaas.payment_gateway.services.transaction;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.account.HoldBalance;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.entities.transaction.Transaction;
import br.com.nimblebaas.payment_gateway.enums.transaction.TransactionPurpose;
import br.com.nimblebaas.payment_gateway.enums.transaction.TransactionStatus;
import br.com.nimblebaas.payment_gateway.enums.transaction.TransactionType;
import br.com.nimblebaas.payment_gateway.repositories.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TransactionService {
    
    private final TransactionRepository transactionRepository;

    public Transaction createDepositTransaction(Account account, BigDecimal amount, String authorizationIdentifier) {
        var transaction = Transaction.builder()
            .partyAccount(account)
            .counterpartAccount(account)
            .amount(amount)
            .type(TransactionType.CREDIT)
            .purpose(TransactionPurpose.DEPOSIT)
            .status(TransactionStatus.PENDING)
            .authorizationIdentifier(authorizationIdentifier)
            .build();
        return transactionRepository.save(transaction);
    }

    public Transaction createChargePaymentDebitTransaction(Charge charge, HoldBalance holdBalance) {
        var transaction = Transaction.builder()
            .partyAccount(charge.getDestinationUser().getAccount())
            .counterpartAccount(charge.getOriginatorUser().getAccount())
            .charge(charge)
            .amount(charge.getAmount())
            .type(TransactionType.DEBIT)
            .purpose(TransactionPurpose.CHARGE_PAYMENT)
            .status(TransactionStatus.PENDING)
            .holdBalance(holdBalance)
            .build();
        return transactionRepository.save(transaction);
    }

    public Transaction createChargeRefundDebitTransaction(Charge charge, HoldBalance holdBalance) {
        var transaction = Transaction.builder()
            .partyAccount(charge.getOriginatorUser().getAccount())
            .counterpartAccount(charge.getDestinationUser().getAccount())
            .charge(charge)
            .amount(charge.getAmount())
            .type(TransactionType.DEBIT)
            .purpose(TransactionPurpose.CHARGE_REFUND)
            .status(TransactionStatus.PENDING)
            .holdBalance(holdBalance)
            .build();
        return transactionRepository.save(transaction);
    }

    public Transaction createChargePaymentCreditTransaction(Charge charge, String authorizationIdentifier) {
        var transaction = Transaction.builder()
            .charge(charge)
            .amount(charge.getAmount())
            .type(TransactionType.CREDIT)
            .purpose(TransactionPurpose.CHARGE_PAYMENT)
            .status(TransactionStatus.PENDING)
            .authorizationIdentifier(authorizationIdentifier)
            .build();
        return transactionRepository.save(transaction);
    }

    public Transaction createChargeRefundCreditTransaction(Charge charge) {
        var transaction = Transaction.builder()
            .partyAccount(charge.getDestinationUser().getAccount())
            .counterpartAccount(charge.getOriginatorUser().getAccount())
            .charge(charge)
            .amount(charge.getAmount())
            .type(TransactionType.CREDIT)
            .purpose(TransactionPurpose.CHARGE_REFUND)
            .status(TransactionStatus.PENDING)
            .build();
        return transactionRepository.save(transaction);
    }

    public void completeSuccessTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);
    }

    public void completeFailedTransaction(Transaction transaction, String errorMessage) {
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setErrorMessage(errorMessage);
        transactionRepository.save(transaction);
    }
}
