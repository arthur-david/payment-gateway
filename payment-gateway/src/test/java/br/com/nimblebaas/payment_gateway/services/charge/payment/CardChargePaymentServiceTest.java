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
import br.com.nimblebaas.payment_gateway.dtos.internal.authorizer.GetAuthorizerDTO;
import br.com.nimblebaas.payment_gateway.dtos.internal.charge.ChargePaymentDTO;
import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.account.HoldBalance;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.entities.charge.ChargePayment;
import br.com.nimblebaas.payment_gateway.entities.transaction.Transaction;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.account.HoldBalanceType;
import br.com.nimblebaas.payment_gateway.enums.authorizer.AuthorizerPurpose;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;
import br.com.nimblebaas.payment_gateway.enums.charge.PaymentMethod;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.repositories.charge.ChargePaymentRepository;
import br.com.nimblebaas.payment_gateway.services.account.AccountService;
import br.com.nimblebaas.payment_gateway.services.account.HoldBalanceService;
import br.com.nimblebaas.payment_gateway.services.authorizer.AuthorizerService;
import br.com.nimblebaas.payment_gateway.services.transaction.TransactionService;

@ExtendWith(MockitoExtension.class)
class CardChargePaymentServiceTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private AuthorizerService authorizerService;

    @Mock
    private AccountService accountService;

    @Mock
    private HoldBalanceService holdBalanceService;

    @Mock
    private ChargePaymentRepository chargePaymentRepository;

    @InjectMocks
    private CardChargePaymentService cardChargePaymentService;

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
            PaymentMethod.CREDIT_CARD,
            "1234567890123456",
            "123",
            "12/2025",
            1
        );
    }

    @Test
    void isResponsible_WithCreditCardMethod_ShouldReturnTrue() {
        boolean result = cardChargePaymentService.isResponsible(PaymentMethod.CREDIT_CARD);

        assertTrue(result);
    }

    @Test
    void isResponsible_WithOtherMethod_ShouldReturnFalse() {
        boolean result = cardChargePaymentService.isResponsible(PaymentMethod.ACCOUNT_BALANCE);

        assertFalse(result);
    }

    @Test
    void pay_WithAuthorization_ShouldPaySuccessfully() {
        ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargePaymentInputRecord, charge);
        Transaction transaction = new Transaction();

        when(authorizerService.authorize(any(AuthorizerPurpose.class), any(GetAuthorizerDTO.class))).thenReturn(true);
        when(transactionService.createChargePaymentCreditTransaction(any(), any())).thenReturn(transaction);
        when(chargePaymentRepository.save(any())).thenReturn(new ChargePayment());

        cardChargePaymentService.pay(chargePaymentDTO);

        verify(authorizerService).authorize(any(AuthorizerPurpose.class), any(GetAuthorizerDTO.class));
        verify(accountService).makeDeposit(originatorAccount, charge.getAmount());
        verify(chargePaymentRepository).save(any(ChargePayment.class));
    }

    @Test
    void pay_WithoutAuthorization_ShouldThrowException() {
        ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargePaymentInputRecord, charge);

        when(authorizerService.authorize(any(AuthorizerPurpose.class), any(GetAuthorizerDTO.class))).thenReturn(false);

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> cardChargePaymentService.pay(chargePaymentDTO)
        );

        assertEquals(BusinessRules.AUTHORIZATION_FAILED.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void cancel_WithAuthorization_ShouldCancelSuccessfully() {
        ChargePayment payment = ChargePayment.builder()
            .charge(charge)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .authorizationIdentifier("auth-id-123")
            .build();
        charge.setPayment(payment);

        HoldBalance holdBalance = new HoldBalance();
        Transaction transaction = new Transaction();

        when(holdBalanceService.createHold(any(), any(), any())).thenReturn(holdBalance);
        when(transactionService.createChargeRefundDebitTransaction(any(), any())).thenReturn(transaction);
        when(authorizerService.authorize(any(AuthorizerPurpose.class), any(GetAuthorizerDTO.class))).thenReturn(true);
        when(transactionService.createChargeRefundCreditTransaction(any())).thenReturn(transaction);
        when(chargePaymentRepository.save(any())).thenReturn(payment);

        cardChargePaymentService.cancel(charge);

        verify(holdBalanceService).createHold(originatorAccount, charge.getAmount(), HoldBalanceType.CHARGE_REFUND);
        verify(authorizerService).authorize(any(AuthorizerPurpose.class), any(GetAuthorizerDTO.class));
        verify(holdBalanceService).confirmHold(holdBalance);
        verify(accountService).makeDeposit(destinationAccount, charge.getAmount());
        verify(chargePaymentRepository).save(payment);
    }

    @Test
    void cancel_WithoutAuthorization_ShouldThrowException() {
        ChargePayment payment = ChargePayment.builder()
            .charge(charge)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .authorizationIdentifier("auth-id-123")
            .build();
        charge.setPayment(payment);

        HoldBalance holdBalance = new HoldBalance();
        Transaction transaction = new Transaction();

        when(holdBalanceService.createHold(any(), any(), any())).thenReturn(holdBalance);
        when(transactionService.createChargeRefundDebitTransaction(any(), any())).thenReturn(transaction);
        when(authorizerService.authorize(any(AuthorizerPurpose.class), any(GetAuthorizerDTO.class))).thenReturn(false);

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> cardChargePaymentService.cancel(charge)
        );

        assertEquals(BusinessRules.AUTHORIZATION_FAILED.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void cancel_WithInsufficientBalance_ShouldThrowException() {
        originatorAccount.setTotalBalance(new BigDecimal("50.00"));
        ChargePayment payment = ChargePayment.builder()
            .charge(charge)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .authorizationIdentifier("auth-id-123")
            .build();
        charge.setPayment(payment);

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> cardChargePaymentService.cancel(charge)
        );

        assertEquals(BusinessRules.INSUFFICIENT_BALANCE.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void pay_WhenDepositFails_ShouldCompleteFailedTransaction() {
        ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargePaymentInputRecord, charge);
        Transaction creditTransaction = new Transaction();

        when(authorizerService.authorize(any(AuthorizerPurpose.class), any(GetAuthorizerDTO.class))).thenReturn(true);
        when(transactionService.createChargePaymentCreditTransaction(any(), any())).thenReturn(creditTransaction);

        BusinessRuleException depositException = new BusinessRuleException(
            CardChargePaymentService.class,
            BusinessRules.ACCOUNT_NOT_FOUND,
            "Conta não encontrada"
        );

        doThrow(depositException).when(accountService).makeDeposit(any(), any());

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> cardChargePaymentService.pay(chargePaymentDTO)
        );

        verify(transactionService).completeFailedTransaction(creditTransaction, exception.getMessage());
        assertEquals(BusinessRules.ACCOUNT_NOT_FOUND.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void cancel_WhenRefundFails_ShouldCancelHoldAndCompleteFailedTransaction() {
        ChargePayment payment = ChargePayment.builder()
            .charge(charge)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .authorizationIdentifier("auth-id-123")
            .build();
        charge.setPayment(payment);

        HoldBalance holdBalance = new HoldBalance();
        Transaction debitTransaction = new Transaction();

        when(holdBalanceService.createHold(any(), any(), any())).thenReturn(holdBalance);
        when(transactionService.createChargeRefundDebitTransaction(any(), any())).thenReturn(debitTransaction);
        when(authorizerService.authorize(any(AuthorizerPurpose.class), any(GetAuthorizerDTO.class))).thenReturn(true);
        when(transactionService.createChargeRefundCreditTransaction(any())).thenReturn(new Transaction());

        BusinessRuleException refundException = new BusinessRuleException(
            CardChargePaymentService.class,
            BusinessRules.ACCOUNT_NOT_FOUND,
            "Conta de destino não encontrada"
        );

        doThrow(refundException).when(accountService).makeDeposit(any(), any());

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> cardChargePaymentService.cancel(charge)
        );

        verify(holdBalanceService).cancelHold(holdBalance);
        verify(transactionService).completeFailedTransaction(debitTransaction, exception.getMessage());
        assertEquals(BusinessRules.ACCOUNT_NOT_FOUND.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void pay_WithSuccessfulPayment_ShouldCompleteSuccessTransaction() {
        ChargePaymentDTO chargePaymentDTO = new ChargePaymentDTO(chargePaymentInputRecord, charge);
        Transaction creditTransaction = new Transaction();

        when(authorizerService.authorize(any(AuthorizerPurpose.class), any(GetAuthorizerDTO.class))).thenReturn(true);
        when(transactionService.createChargePaymentCreditTransaction(any(), any())).thenReturn(creditTransaction);
        when(chargePaymentRepository.save(any())).thenReturn(new ChargePayment());

        cardChargePaymentService.pay(chargePaymentDTO);

        verify(transactionService).completeSuccessTransaction(creditTransaction);
    }

    @Test
    void cancel_WithSuccessfulRefund_ShouldCompleteSuccessTransaction() {
        ChargePayment payment = ChargePayment.builder()
            .charge(charge)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .authorizationIdentifier("auth-id-123")
            .build();
        charge.setPayment(payment);

        HoldBalance holdBalance = new HoldBalance();
        Transaction debitTransaction = new Transaction();

        when(holdBalanceService.createHold(any(), any(), any())).thenReturn(holdBalance);
        when(transactionService.createChargeRefundDebitTransaction(any(), any())).thenReturn(debitTransaction);
        when(authorizerService.authorize(any(AuthorizerPurpose.class), any(GetAuthorizerDTO.class))).thenReturn(true);
        when(transactionService.createChargeRefundCreditTransaction(any())).thenReturn(new Transaction());
        when(chargePaymentRepository.save(any())).thenReturn(payment);

        cardChargePaymentService.cancel(charge);

        verify(transactionService).completeSuccessTransaction(debitTransaction);
    }
}

