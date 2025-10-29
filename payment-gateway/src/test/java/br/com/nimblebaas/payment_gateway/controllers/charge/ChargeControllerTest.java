package br.com.nimblebaas.payment_gateway.controllers.charge;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.nimblebaas.payment_gateway.configs.security.SecurityConfig;
import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargeCancelInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargeInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.input.charge.ChargePaymentInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.charge.ChargeOutputDTO;
import br.com.nimblebaas.payment_gateway.enums.charge.ChargeStatus;
import br.com.nimblebaas.payment_gateway.enums.charge.PaymentMethod;
import br.com.nimblebaas.payment_gateway.filters.authentication.AuthenticationFilter;
import br.com.nimblebaas.payment_gateway.services.charge.ChargeService;

@WebMvcTest(
    controllers = ChargeController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, AuthenticationFilter.class}
    )
)
@AutoConfigureMockMvc(addFilters = false)
class ChargeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChargeService chargeService;

    @Test
    @WithMockUser
    void create_WithValidData_ShouldReturnCreated() throws Exception {
        ChargeInputRecord chargeInput = new ChargeInputRecord(
            "12345678900",
            new BigDecimal("100.00"),
            "Test charge"
        );

        ChargeOutputDTO chargeOutput = new ChargeOutputDTO();
        chargeOutput.setIdentifier("charge-123");
        chargeOutput.setOriginatorCpf("98765432100");
        chargeOutput.setDestinationCpf("12345678900");
        chargeOutput.setAmount(new BigDecimal("100.00"));
        chargeOutput.setDescription("Test charge");
        chargeOutput.setStatus(ChargeStatus.PENDING);
        chargeOutput.setCreatedAt(LocalDateTime.now());

        when(chargeService.create(any(), any())).thenReturn(chargeOutput);

        mockMvc.perform(post(ChargeRoutes.CREATE.getPath())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chargeInput)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identifier").value("charge-123"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(chargeService).create(any(), any());
    }

    @Test
    @WithMockUser
    void getSentCharges_ShouldReturnOk() throws Exception {
        when(chargeService.getSentChargesByUser(any(), any())).thenReturn(List.of());

        mockMvc.perform(get(ChargeRoutes.SENT.getPath())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getSentCharges_WithStatusFilter_ShouldReturnFilteredCharges() throws Exception {
        ChargeOutputDTO charge1 = new ChargeOutputDTO();
        charge1.setIdentifier("charge-1");
        charge1.setStatus(ChargeStatus.PENDING);
        charge1.setAmount(new BigDecimal("100.00"));

        List<ChargeOutputDTO> charges = List.of(charge1);

        when(chargeService.getSentChargesByUser(any(), anyList())).thenReturn(charges);

        mockMvc.perform(get(ChargeRoutes.SENT.getPath())
                .param("statuses", "PENDING")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(chargeService).getSentChargesByUser(any(), anyList());
    }

    @Test
    @WithMockUser
    void getReceivedCharges_ShouldReturnOk() throws Exception {
        when(chargeService.getReceivedChargesByUser(any(), any())).thenReturn(List.of());

        mockMvc.perform(get(ChargeRoutes.RECEIVED.getPath())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getReceivedCharges_WithStatusFilter_ShouldReturnFilteredCharges() throws Exception {
        ChargeOutputDTO charge1 = new ChargeOutputDTO();
        charge1.setIdentifier("charge-1");
        charge1.setStatus(ChargeStatus.PAID);
        charge1.setAmount(new BigDecimal("150.00"));

        List<ChargeOutputDTO> charges = List.of(charge1);

        when(chargeService.getReceivedChargesByUser(any(), anyList())).thenReturn(charges);

        mockMvc.perform(get(ChargeRoutes.RECEIVED.getPath())
                .param("statuses", "PAID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PAID"));

        verify(chargeService).getReceivedChargesByUser(any(), anyList());
    }

    @Test
    @WithMockUser
    void pay_WithAccountBalance_ShouldReturnNoContent() throws Exception {
        ChargePaymentInputRecord paymentInput = new ChargePaymentInputRecord(
            "charge-123",
            PaymentMethod.ACCOUNT_BALANCE,
            null,
            null,
            null,
            null
        );

        doNothing().when(chargeService).pay(any(), any());

        mockMvc.perform(post(ChargeRoutes.PAY.getPath())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentInput)))
                .andExpect(status().isNoContent());

        verify(chargeService).pay(any(), any());
    }

    @Test
    @WithMockUser
    void pay_WithCreditCard_ShouldReturnNoContent() throws Exception {
        ChargePaymentInputRecord paymentInput = new ChargePaymentInputRecord(
            "charge-123",
            PaymentMethod.CREDIT_CARD,
            "1234567812345678",
            "12/25",
            "123",
            1
        );

        doNothing().when(chargeService).pay(any(), any());

        mockMvc.perform(post(ChargeRoutes.PAY.getPath())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentInput)))
                .andExpect(status().isNoContent());

        verify(chargeService).pay(any(), any());
    }

    @Test
    @WithMockUser
    void cancel_WithValidIdentifier_ShouldReturnNoContent() throws Exception {
        ChargeCancelInputRecord cancelInput = new ChargeCancelInputRecord("charge-123");

        doNothing().when(chargeService).cancel(any(), any());

        mockMvc.perform(post(ChargeRoutes.CANCEL.getPath())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelInput)))
                .andExpect(status().isNoContent());

        verify(chargeService).cancel(any(), any());
    }
}

