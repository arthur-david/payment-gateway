package br.com.nimblebaas.payment_gateway.services.charge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.nimblebaas.payment_gateway.configs.authentication.UserAuthenticated;
import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargeCancelInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargeInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargePaymentInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.internal.charge.ChargePaymentDTO;
import br.com.nimblebaas.payment_gateway.dtos.output.charge.ChargeOutputDTO;
import br.com.nimblebaas.payment_gateway.entities.account.Account;
import br.com.nimblebaas.payment_gateway.entities.charge.Charge;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;
import br.com.nimblebaas.payment_gateway.enums.charge.PaymentMethod;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.repositories.charge.ChargeRepository;
import br.com.nimblebaas.payment_gateway.services.charge.payment.ChargePaymentService;
import br.com.nimblebaas.payment_gateway.services.user.UserService;

@ExtendWith(MockitoExtension.class)
class ChargeServiceTest {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private UserService userService;

    @Mock
    private ChargePaymentService chargePaymentService;

    @InjectMocks
    private ChargeService chargeService;

    private User originatorUser;
    private User destinationUser;
    private UserAuthenticated userAuthenticated;
    private Account originatorAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        originatorUser = new User();
        originatorUser.setCpf("12345678900");
        originatorUser.setEmail("originator@example.com");

        destinationUser = new User();
        destinationUser.setCpf("98765432100");
        destinationUser.setEmail("destination@example.com");

        originatorAccount = new Account(originatorUser);
        destinationAccount = new Account(destinationUser);
        originatorUser.setAccount(originatorAccount);
        destinationUser.setAccount(destinationAccount);

        userAuthenticated = new UserAuthenticated(originatorUser);
    }

    @Test
    void create_WithValidData_ShouldCreateCharge() {
        ChargeInputRecord chargeInput = new ChargeInputRecord(
            "98765432100",
            new BigDecimal("100.00"),
            "Test charge"
        );

        Charge charge = Charge.builder()
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PENDING)
            .build();

        when(userService.getUserByCpf(anyString())).thenReturn(destinationUser);
        when(chargeRepository.save(any(Charge.class))).thenReturn(charge);

        ChargeOutputDTO result = chargeService.create(userAuthenticated, chargeInput);

        assertNotNull(result);
        verify(chargeRepository).save(any(Charge.class));
    }

    @Test
    void create_WithSameOriginatorAndDestination_ShouldThrowException() {
        ChargeInputRecord chargeInput = new ChargeInputRecord(
            "12345678900",
            new BigDecimal("100.00"),
            "Test charge"
        );

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> chargeService.create(userAuthenticated, chargeInput)
        );

        assertEquals(BusinessRules.CHARGE_DESTINATION_SAME_AS_ORIGINATOR.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void getSentChargesByUser_ShouldReturnCharges() {
        Charge charge = Charge.builder()
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PENDING)
            .build();

        when(chargeRepository.findByOriginatorUserAndStatusIn(any(User.class), anyList()))
            .thenReturn(Arrays.asList(charge));

        List<ChargeOutputDTO> result = chargeService.getSentChargesByUser(
            userAuthenticated,
            Arrays.asList(ChargeStatus.PENDING)
        );

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getReceivedChargesByUser_ShouldReturnCharges() {
        Charge charge = Charge.builder()
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PENDING)
            .build();

        when(chargeRepository.findByDestinationUserAndStatusIn(any(User.class), anyList()))
            .thenReturn(Arrays.asList(charge));

        List<ChargeOutputDTO> result = chargeService.getReceivedChargesByUser(
            userAuthenticated,
            Arrays.asList(ChargeStatus.PENDING)
        );

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void pay_WithValidData_ShouldPayCharge() {
        ChargePaymentInputRecord paymentInput = new ChargePaymentInputRecord(
            "charge-identifier",
            PaymentMethod.ACCOUNT_BALANCE,
            null, null, null, null
        );

        Charge charge = Charge.builder()
            .identifier("charge-identifier")
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PENDING)
            .build();

        UserAuthenticated destinationAuthenticated = new UserAuthenticated(destinationUser);

        when(chargeRepository.findByIdentifier(anyString())).thenReturn(Optional.of(charge));
        when(chargeRepository.save(any(Charge.class))).thenReturn(charge);

        chargeService.pay(destinationAuthenticated, paymentInput);

        assertEquals(ChargeStatus.PAID, charge.getStatus());
        verify(chargeRepository).save(charge);
    }

    @Test
    void pay_WithInvalidUser_ShouldThrowException() {
        ChargePaymentInputRecord paymentInput = new ChargePaymentInputRecord(
            "charge-identifier",
            PaymentMethod.ACCOUNT_BALANCE,
            null, null, null, null
        );

        Charge charge = Charge.builder()
            .identifier("charge-identifier")
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PENDING)
            .build();

        when(chargeRepository.findByIdentifier(anyString())).thenReturn(Optional.of(charge));

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> chargeService.pay(userAuthenticated, paymentInput)
        );

        assertEquals(BusinessRules.CHARGE_NOT_ALLOWED_TO_PAY.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void pay_WithInvalidStatus_ShouldThrowException() {
        ChargePaymentInputRecord paymentInput = new ChargePaymentInputRecord(
            "charge-identifier",
            PaymentMethod.ACCOUNT_BALANCE,
            null, null, null, null
        );

        Charge charge = Charge.builder()
            .identifier("charge-identifier")
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PAID)
            .build();

        UserAuthenticated destinationAuthenticated = new UserAuthenticated(destinationUser);

        when(chargeRepository.findByIdentifier(anyString())).thenReturn(Optional.of(charge));

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> chargeService.pay(destinationAuthenticated, paymentInput)
        );

        assertEquals(BusinessRules.CHARGE_NOT_ALLOWED_TO_PAY.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void cancel_WithPendingCharge_ShouldCancelDirectly() {
        ChargeCancelInputRecord cancelInput = new ChargeCancelInputRecord("charge-identifier");

        Charge charge = Charge.builder()
            .identifier("charge-identifier")
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PENDING)
            .build();

        when(chargeRepository.findByIdentifier(anyString())).thenReturn(Optional.of(charge));
        when(chargeRepository.save(any(Charge.class))).thenReturn(charge);

        chargeService.cancel(userAuthenticated, cancelInput);

        assertEquals(ChargeStatus.CANCELLED, charge.getStatus());
        verify(chargeRepository).save(charge);
    }

    @Test
    void cancel_WithInvalidUser_ShouldThrowException() {
        ChargeCancelInputRecord cancelInput = new ChargeCancelInputRecord("charge-identifier");

        Charge charge = Charge.builder()
            .identifier("charge-identifier")
            .originatorUser(destinationUser)
            .destinationUser(originatorUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PENDING)
            .build();

        when(chargeRepository.findByIdentifier(anyString())).thenReturn(Optional.of(charge));

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> chargeService.cancel(userAuthenticated, cancelInput)
        );

        assertEquals(BusinessRules.CHARGE_NOT_ALLOWED_TO_CANCEL.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void cancel_WithInvalidStatus_ShouldThrowException() {
        ChargeCancelInputRecord cancelInput = new ChargeCancelInputRecord("charge-identifier");

        Charge charge = Charge.builder()
            .identifier("charge-identifier")
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.CANCELLED)
            .build();

        when(chargeRepository.findByIdentifier(anyString())).thenReturn(Optional.of(charge));

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> chargeService.cancel(userAuthenticated, cancelInput)
        );

        assertEquals(BusinessRules.CHARGE_NOT_ALLOWED_TO_CANCEL.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void pay_WithChargeNotFound_ShouldThrowException() {
        ChargePaymentInputRecord paymentInput = new ChargePaymentInputRecord(
            "non-existent-identifier",
            PaymentMethod.ACCOUNT_BALANCE,
            null, null, null, null
        );

        when(chargeRepository.findByIdentifier(anyString())).thenReturn(Optional.empty());

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> chargeService.pay(userAuthenticated, paymentInput)
        );

        assertEquals(BusinessRules.CHARGE_NOT_FOUND.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void cancel_WithChargeNotFound_ShouldThrowException() {
        ChargeCancelInputRecord cancelInput = new ChargeCancelInputRecord("non-existent-identifier");

        when(chargeRepository.findByIdentifier(anyString())).thenReturn(Optional.empty());

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> chargeService.cancel(userAuthenticated, cancelInput)
        );

        assertEquals(BusinessRules.CHARGE_NOT_FOUND.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void getSentChargesByUser_WithNullStatuses_ShouldReturnAllStatuses() {
        Charge charge = Charge.builder()
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PENDING)
            .build();

        when(chargeRepository.findByOriginatorUserAndStatusIn(any(User.class), anyList()))
            .thenReturn(Arrays.asList(charge));

        List<ChargeOutputDTO> result = chargeService.getSentChargesByUser(userAuthenticated, null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getSentChargesByUser_WithEmptyStatuses_ShouldReturnAllStatuses() {
        Charge charge = Charge.builder()
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PAID)
            .build();

        when(chargeRepository.findByOriginatorUserAndStatusIn(any(User.class), anyList()))
            .thenReturn(Arrays.asList(charge));

        List<ChargeOutputDTO> result = chargeService.getSentChargesByUser(userAuthenticated, Arrays.asList());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getReceivedChargesByUser_WithNullStatuses_ShouldReturnAllStatuses() {
        Charge charge = Charge.builder()
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.CANCELLED)
            .build();

        when(chargeRepository.findByDestinationUserAndStatusIn(any(User.class), anyList()))
            .thenReturn(Arrays.asList(charge));

        List<ChargeOutputDTO> result = chargeService.getReceivedChargesByUser(userAuthenticated, null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getReceivedChargesByUser_WithEmptyStatuses_ShouldReturnAllStatuses() {
        Charge charge = Charge.builder()
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PAID)
            .build();

        when(chargeRepository.findByDestinationUserAndStatusIn(any(User.class), anyList()))
            .thenReturn(Arrays.asList(charge));

        List<ChargeOutputDTO> result = chargeService.getReceivedChargesByUser(userAuthenticated, Arrays.asList());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void pay_WhenPaymentServiceThrowsBusinessRuleException_ShouldSetStatusToPaymentFailed() {
        ChargePaymentInputRecord paymentInput = new ChargePaymentInputRecord(
            "charge-identifier",
            PaymentMethod.ACCOUNT_BALANCE,
            null, null, null, null
        );

        Charge charge = Charge.builder()
            .identifier("charge-identifier")
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PENDING)
            .build();

        UserAuthenticated destinationAuthenticated = new UserAuthenticated(destinationUser);

        when(chargeRepository.findByIdentifier(anyString())).thenReturn(Optional.of(charge));
        
        BusinessRuleException paymentException = new BusinessRuleException(
            ChargeService.class, 
            BusinessRules.INSUFFICIENT_BALANCE, 
            "Saldo insuficiente"
        );
        
        doThrow(paymentException).when(chargePaymentService).pay(any(ChargePaymentDTO.class), any(PaymentMethod.class));

        assertThrows(
            BusinessRuleException.class,
            () -> chargeService.pay(destinationAuthenticated, paymentInput)
        );

        assertEquals(ChargeStatus.PAYMENT_FAILED, charge.getStatus());
        assertEquals("Saldo insuficiente", charge.getErrorMessage());
        verify(chargeRepository).save(charge);
    }

    @Test
    void pay_WhenPaymentServiceThrowsGenericException_ShouldSetStatusToPaymentFailedAndThrowBusinessRuleException() {
        ChargePaymentInputRecord paymentInput = new ChargePaymentInputRecord(
            "charge-identifier",
            PaymentMethod.CREDIT_CARD,
            null, null, null, null
        );

        Charge charge = Charge.builder()
            .identifier("charge-identifier")
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PENDING)
            .build();

        UserAuthenticated destinationAuthenticated = new UserAuthenticated(destinationUser);

        when(chargeRepository.findByIdentifier(anyString())).thenReturn(Optional.of(charge));
        doThrow(new RuntimeException("Erro inesperado")).when(chargePaymentService).pay(any(ChargePaymentDTO.class), any(PaymentMethod.class));

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> chargeService.pay(destinationAuthenticated, paymentInput)
        );

        assertEquals(ChargeStatus.PAYMENT_FAILED, charge.getStatus());
        assertEquals("Erro inesperado", charge.getErrorMessage());
        assertEquals(BusinessRules.CHARGE_PAYMENT_ERROR.name(), exception.getErrorDTO().getReason());
        verify(chargeRepository).save(charge);
    }

    @Test
    void cancel_WithPaidCharge_WhenCancelServiceThrowsBusinessRuleException_ShouldSetStatusToCancelledFailed() {
        ChargeCancelInputRecord cancelInput = new ChargeCancelInputRecord("charge-identifier");

        Charge charge = Charge.builder()
            .identifier("charge-identifier")
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PAID)
            .build();

        when(chargeRepository.findByIdentifier(anyString())).thenReturn(Optional.of(charge));
        
        BusinessRuleException cancelException = new BusinessRuleException(
            ChargeService.class, 
            BusinessRules.AUTHORIZATION_FAILED, 
            "Autorização negada"
        );
        
        doThrow(cancelException).when(chargePaymentService).cancel(any(Charge.class));

        assertThrows(
            BusinessRuleException.class,
            () -> chargeService.cancel(userAuthenticated, cancelInput)
        );

        assertEquals(ChargeStatus.CANCELLED_FAILED, charge.getStatus());
        assertEquals("Autorização negada", charge.getErrorMessage());
        verify(chargeRepository).save(charge);
    }

    @Test
    void cancel_WithPaidCharge_WhenCancelServiceThrowsGenericException_ShouldSetStatusToCancelledFailedAndThrowBusinessRuleException() {
        ChargeCancelInputRecord cancelInput = new ChargeCancelInputRecord("charge-identifier");

        Charge charge = Charge.builder()
            .identifier("charge-identifier")
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PAID)
            .build();

        when(chargeRepository.findByIdentifier(anyString())).thenReturn(Optional.of(charge));
        doThrow(new RuntimeException("Erro de conexão")).when(chargePaymentService).cancel(any(Charge.class));

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> chargeService.cancel(userAuthenticated, cancelInput)
        );

        assertEquals(ChargeStatus.CANCELLED_FAILED, charge.getStatus());
        assertEquals("Erro de conexão", charge.getErrorMessage());
        assertEquals(BusinessRules.CHARGE_CANCEL_ERROR.name(), exception.getErrorDTO().getReason());
        verify(chargeRepository).save(charge);
    }

    @Test
    void cancel_WithPaidCharge_WhenSuccessful_ShouldSetStatusToCancelled() {
        ChargeCancelInputRecord cancelInput = new ChargeCancelInputRecord("charge-identifier");

        Charge charge = Charge.builder()
            .identifier("charge-identifier")
            .originatorUser(originatorUser)
            .destinationUser(destinationUser)
            .amount(new BigDecimal("100.00"))
            .status(ChargeStatus.PAID)
            .build();

        when(chargeRepository.findByIdentifier(anyString())).thenReturn(Optional.of(charge));
        when(chargeRepository.save(any(Charge.class))).thenReturn(charge);

        chargeService.cancel(userAuthenticated, cancelInput);

        assertEquals(ChargeStatus.CANCELLED, charge.getStatus());
        verify(chargePaymentService).cancel(charge);
        verify(chargeRepository).save(charge);
    }
}

