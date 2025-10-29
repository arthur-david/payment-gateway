package br.com.nimblebaas.payment_gateway.services.charge.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargePaymentInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.internal.charge.ChargePaymentDTO;
import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.account.HoldBalance;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.entities.charge.ChargePayment;
import br.com.nimblebaas.payment_gateway.entities.transaction.Transaction;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.account.HoldBalanceType;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;
import br.com.nimblebaas.payment_gateway.enums.charge.PaymentMethod;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.repositories.charge.ChargePaymentRepository;
import br.com.nimblebaas.payment_gateway.services.account.AccountService;
import br.com.nimblebaas.payment_gateway.services.account.HoldBalanceService;
import br.com.nimblebaas.payment_gateway.services.transaction.TransactionService;

@ExtendWith(MockitoExtension.class)
class BalanceChargePaymentServiceTest {

    @Mock
    private HoldBalanceService holdBalanceService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountService accountService;

    @Mock
    private ChargePaymentRepository chargePaymentRepository;

    @InjectMocks
    private BalanceChargePaymentService balanceChargePaymentService;

    private User originatorUser;
    private User destinationUser;
    private Account originatorAccount;
    private Account destinationAccount;
    private Charge charge;
    private ChargePaymentInputRecord chargePaymentInputRecord;

    @BeforeEach
    void setUp() {
        originatorUser = new User();
        originatorUser.setCpf("12345678900");

        destinationUser = new User();
        destinationUser.setCpf("98765432100");

        originatorAccount = new Account(originatorUser);
        originatorAccount.setTotalBalance(new BigDecimal("1000.00"));

        destinationAccount = new Account(destinationUser);
        destinationAccount.setTotalBalance(new BigDecimal("1000.00"));

        originatorUser.setAccount(originatorAccount);
        destinationUser.setAccount(destinationAccount);

        charge = Charge.builder()
            .identifier("charge-identifier")
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PENDING)
            .build();

        chargePaymentInputRecord = new ChargePaymentInputRecord(
            "charge-identifier",
            PaymentMethod.ACCOUNT_BALANCE,
            null, null, null, null
        );
    }

    @Test
    void isResponsible_WithAccountBalanceMethod_ShouldReturnTrue() {
        boolean result = balanceChargePaymentService.isResponsible(PaymentMethod.ACCOUNT_BALANCE);

        assertTrue(result);
    }

    @Test
    void isResponsible_WithOtherMethod_ShouldReturnFalse() {
        boolean result = balanceChargePaymentService.isResponsible(PaymentMethod.CREDIT_CARD);

        assertFalse(result);
    }

    @Test
    void pay_WithSufficientBalance_ShouldPaySuccessfully() {
        ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargePaymentInputRecord, charge);
        HoldBalance holdBalance = new HoldBalance();
        Transaction transaction = new Transaction();

        when(holdBalanceService.createHold(any(), any(), any())).thenReturn(holdBalance);
        when(transactionService.createChargePaymentDebitTransaction(any(), any())).thenReturn(transaction);
        when(transactionService.createChargePaymentCreditTransaction(any(), any())).thenReturn(transaction);
        when(chargePaymentRepository.save(any())).thenReturn(new ChargePayment());

        balanceChargePaymentService.pay(chargePaymentDTO);

        verify(holdBalanceService).createHold(destinationAccount, charge.getAmount(), HoldBalanceType.CHARGE_PAYMENT);
        verify(holdBalanceService).confirmHold(holdBalance);
        verify(accountService).makeDeposit(originatorAccount, charge.getAmount());
        verify(chargePaymentRepository).save(any(ChargePayment.class));
    }

    @Test
    void pay_WithInsufficientBalance_ShouldThrowException() {
        destinationAccount.setTotalBalance(new BigDecimal("50.00"));
        ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargePaymentInputRecord, charge);

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> balanceChargePaymentService.pay(chargePaymentDTO)
        );

        assertEquals(BusinessRules.INSUFFICIENT_BALANCE.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void cancel_WithSufficientBalance_ShouldCancelSuccessfully() {
        ChargePayment payment = ChargePayment.builder()
            .charge(charge)
            .paymentMethod(PaymentMethod.ACCOUNT_BALANCE)
            .build();
        charge.setPayment(payment);

        HoldBalance holdBalance = new HoldBalance();
        Transaction transaction = new Transaction();

        when(holdBalanceService.createHold(any(), any(), any())).thenReturn(holdBalance);
        when(transactionService.createChargeRefundDebitTransaction(any(), any())).thenReturn(transaction);
        when(transactionService.createChargeRefundCreditTransaction(any())).thenReturn(transaction);
        when(chargePaymentRepository.save(any())).thenReturn(payment);

        balanceChargePaymentService.cancel(charge);

        verify(holdBalanceService).createHold(originatorAccount, charge.getAmount(), HoldBalanceType.CHARGE_REFUND);
        verify(holdBalanceService).confirmHold(holdBalance);
        verify(accountService).makeDeposit(destinationAccount, charge.getAmount());
        verify(chargePaymentRepository).save(payment);
    }

    @Test
    void cancel_WithInsufficientBalance_ShouldThrowException() {
        originatorAccount.setTotalBalance(new BigDecimal("50.00"));
        ChargePayment payment = ChargePayment.builder()
            .charge(charge)
            .paymentMethod(PaymentMethod.ACCOUNT_BALANCE)
            .build();
        charge.setPayment(payment);

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> balanceChargePaymentService.cancel(charge)
        );

        assertEquals(BusinessRules.INSUFFICIENT_BALANCE.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void pay_WhenDepositFails_ShouldCancelHoldAndCompleteFailedTransaction() {
        ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargePaymentInputRecord, charge);
        HoldBalance holdBalance = new HoldBalance();
        Transaction debitTransaction = new Transaction();

        when(holdBalanceService.createHold(any(), any(), any())).thenReturn(holdBalance);
        when(transactionService.createChargePaymentDebitTransaction(any(), any())).thenReturn(debitTransaction);
        when(transactionService.createChargePaymentCreditTransaction(any(), any())).thenReturn(new Transaction());

        BusinessRuleException depositException = new BusinessRuleException(
            BalanceChargePaymentService.class,
            BusinessRules.ACCOUNT_NOT_FOUND,
            "Conta não encontrada"
        );

        doThrow(depositException).when(accountService).makeDeposit(any(), any());

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> balanceChargePaymentService.pay(chargePaymentDTO)
        );

        verify(holdBalanceService).cancelHold(holdBalance);
        verify(transactionService).completeFailedTransaction(debitTransaction, exception.getMessage());
        assertEquals(BusinessRules.ACCOUNT_NOT_FOUND.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void cancel_WhenRefundFails_ShouldCancelHoldAndCompleteFailedTransaction() {
        ChargePayment payment = ChargePayment.builder()
            .charge(charge)
            .paymentMethod(PaymentMethod.ACCOUNT_BALANCE)
            .build();
        charge.setPayment(payment);

        HoldBalance holdBalance = new HoldBalance();
        Transaction debitTransaction = new Transaction();

        when(holdBalanceService.createHold(any(), any(), any())).thenReturn(holdBalance);
        when(transactionService.createChargeRefundDebitTransaction(any(), any())).thenReturn(debitTransaction);
        when(transactionService.createChargeRefundCreditTransaction(any())).thenReturn(new Transaction());

        BusinessRuleException refundException = new BusinessRuleException(
            BalanceChargePaymentService.class,
            BusinessRules.ACCOUNT_NOT_FOUND,
            "Conta de destino não encontrada"
        );

        doThrow(refundException).when(accountService).makeDeposit(any(), any());

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> balanceChargePaymentService.cancel(charge)
        );

        verify(holdBalanceService).cancelHold(holdBalance);
        verify(transactionService).completeFailedTransaction(debitTransaction, exception.getMessage());
        assertEquals(BusinessRules.ACCOUNT_NOT_FOUND.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void pay_WithSuccessfulPayment_ShouldCompleteSuccessTransaction() {
        ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargePaymentInputRecord, charge);
        HoldBalance holdBalance = new HoldBalance();
        Transaction debitTransaction = new Transaction();
        Transaction creditTransaction = new Transaction();

        when(holdBalanceService.createHold(any(), any(), any())).thenReturn(holdBalance);
        when(transactionService.createChargePaymentDebitTransaction(any(), any())).thenReturn(debitTransaction);
        when(transactionService.createChargePaymentCreditTransaction(any(), any())).thenReturn(creditTransaction);
        when(chargePaymentRepository.save(any())).thenReturn(new ChargePayment());

        balanceChargePaymentService.pay(chargePaymentDTO);

        verify(transactionService).completeSuccessTransaction(debitTransaction);
        verify(transactionService).completeSuccessTransaction(creditTransaction);
    }

    @Test
    void cancel_WithSuccessfulRefund_ShouldCompleteSuccessTransaction() {
        ChargePayment payment = ChargePayment.builder()
            .charge(charge)
            .paymentMethod(PaymentMethod.ACCOUNT_BALANCE)
            .build();
        charge.setPayment(payment);

        HoldBalance holdBalance = new HoldBalance();
        Transaction debitTransaction = new Transaction();

        when(holdBalanceService.createHold(any(), any(), any())).thenReturn(holdBalance);
        when(transactionService.createChargeRefundDebitTransaction(any(), any())).thenReturn(debitTransaction);
        when(transactionService.createChargeRefundCreditTransaction(any())).thenReturn(new Transaction());
        when(chargePaymentRepository.save(any())).thenReturn(payment);

        balanceChargePaymentService.cancel(charge);

        verify(transactionService).completeSuccessTransaction(debitTransaction);
    }
}

