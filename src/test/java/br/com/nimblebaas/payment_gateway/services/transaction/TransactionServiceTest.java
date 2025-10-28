package br.com.nimblebaas.payment_gateway.services.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.account.HoldBalance;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.entities.transaction.Transaction;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;
import br.com.nimblebaas.payment_gateway.enums.transaction.TransactionPurpose;
import br.com.nimblebaas.payment_gateway.enums.transaction.TransactionStatus;
import br.com.nimblebaas.payment_gateway.enums.transaction.TransactionType;
import br.com.nimblebaas.payment_gateway.repositories.transaction.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User originatorUser;
    private User destinationUser;
    private Account originatorAccount;
    private Account destinationAccount;
    private Charge charge;
    private HoldBalance holdBalance;

    @BeforeEach
    void setUp() {
        originatorUser = new User();
        originatorUser.setCpf("12345678900");

        destinationUser = new User();
        destinationUser.setCpf("98765432100");

        originatorAccount = new Account(originatorUser);
        destinationAccount = new Account(destinationUser);

        originatorUser.setAccount(originatorAccount);
        destinationUser.setAccount(destinationAccount);

        charge = Charge.builder()
            .identifier("charge-identifier")
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PENDING)
            .build();

        holdBalance = new HoldBalance();
    }

    @Test
    void createDepositTransaction_ShouldCreateTransaction() {
        Transaction transaction = Transaction.builder()
            .partyAccount(originatorAccount)
            .counterpartAccount(originatorAccount)
            .amount(new BigDecimal("100.00"))
            .type(TransactionType.CREDIT)
            .purpose(TransactionPurpose.DEPOSIT)
            .status(TransactionStatus.PENDING)
            .authorizationIdentifier("auth-id-123")
            .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.createDepositTransaction(
            originatorAccount,
            new BigDecimal("100.00"),
            "auth-id-123"
        );

        assertNotNull(result);
        assertEquals(TransactionType.CREDIT, result.getType());
        assertEquals(TransactionPurpose.DEPOSIT, result.getPurpose());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createChargePaymentDebitTransaction_ShouldCreateTransaction() {
        Transaction transaction = Transaction.builder()
            .partyAccount(destinationAccount)
            .counterpartAccount(originatorAccount)
            .charge(charge)
            .amount(charge.getAmount())
            .type(TransactionType.DEBIT)
            .purpose(TransactionPurpose.CHARGE_PAYMENT)
            .status(TransactionStatus.PENDING)
            .holdBalance(holdBalance)
            .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.createChargePaymentDebitTransaction(charge, holdBalance);

        assertNotNull(result);
        assertEquals(TransactionType.DEBIT, result.getType());
        assertEquals(TransactionPurpose.CHARGE_PAYMENT, result.getPurpose());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createChargeRefundDebitTransaction_ShouldCreateTransaction() {
        Transaction transaction = Transaction.builder()
            .partyAccount(originatorAccount)
            .counterpartAccount(destinationAccount)
            .charge(charge)
            .amount(charge.getAmount())
            .type(TransactionType.DEBIT)
            .purpose(TransactionPurpose.CHARGE_REFUND)
            .status(TransactionStatus.PENDING)
            .holdBalance(holdBalance)
            .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.createChargeRefundDebitTransaction(charge, holdBalance);

        assertNotNull(result);
        assertEquals(TransactionType.DEBIT, result.getType());
        assertEquals(TransactionPurpose.CHARGE_REFUND, result.getPurpose());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createChargePaymentCreditTransaction_ShouldCreateTransaction() {
        Transaction transaction = Transaction.builder()
            .charge(charge)
            .amount(charge.getAmount())
            .type(TransactionType.CREDIT)
            .purpose(TransactionPurpose.CHARGE_PAYMENT)
            .status(TransactionStatus.PENDING)
            .authorizationIdentifier("auth-id-123")
            .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.createChargePaymentCreditTransaction(charge, "auth-id-123");

        assertNotNull(result);
        assertEquals(TransactionType.CREDIT, result.getType());
        assertEquals(TransactionPurpose.CHARGE_PAYMENT, result.getPurpose());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createChargeRefundCreditTransaction_ShouldCreateTransaction() {
        Transaction transaction = Transaction.builder()
            .partyAccount(destinationAccount)
            .counterpartAccount(originatorAccount)
            .charge(charge)
            .amount(charge.getAmount())
            .type(TransactionType.CREDIT)
            .purpose(TransactionPurpose.CHARGE_REFUND)
            .status(TransactionStatus.PENDING)
            .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.createChargeRefundCreditTransaction(charge);

        assertNotNull(result);
        assertEquals(TransactionType.CREDIT, result.getType());
        assertEquals(TransactionPurpose.CHARGE_REFUND, result.getPurpose());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void completeSuccessTransaction_ShouldUpdateTransactionStatus() {
        Transaction transaction = Transaction.builder()
            .status(TransactionStatus.PENDING)
            .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        transactionService.completeSuccessTransaction(transaction);

        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void completeFailedTransaction_ShouldUpdateTransactionStatusAndErrorMessage() {
        Transaction transaction = Transaction.builder()
            .status(TransactionStatus.PENDING)
            .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        transactionService.completeFailedTransaction(transaction, "Error occurred");

        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
        assertEquals("Error occurred", transaction.getErrorMessage());
        verify(transactionRepository).save(transaction);
    }
}

