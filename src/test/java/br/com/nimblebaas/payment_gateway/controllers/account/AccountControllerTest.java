package br.com.nimblebaas.payment_gateway.controllers.account;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

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
import br.com.nimblebaas.payment_gateway.dtos.input.account.MakeSelfDepositInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.account.BalanceOutputRecord;
import br.com.nimblebaas.payment_gateway.filters.authentication.AuthenticationFilter;
import br.com.nimblebaas.payment_gateway.services.account.AccountService;

@WebMvcTest(
    controllers = AccountController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, AuthenticationFilter.class}
    )
)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @Test
    @WithMockUser
    void getBalance_ShouldReturnBalance() throws Exception {
        BalanceOutputRecord balanceOutput = new BalanceOutputRecord(
            new BigDecimal("1000.00"),
            new BigDecimal("100.00"),
            new BigDecimal("900.00")
        );

        when(accountService.getBalance(any())).thenReturn(balanceOutput);

        mockMvc.perform(get(AccountRoutes.GET_BALANCE.getPath())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBalance").value(1000.00))
                .andExpect(jsonPath("$.holdBalance").value(100.00))
                .andExpect(jsonPath("$.availableBalance").value(900.00));

        verify(accountService).getBalance(any());
    }

    @Test
    @WithMockUser
    void makeSelfDeposit_ShouldReturnNoContent() throws Exception {
        MakeSelfDepositInputRecord depositInput = new MakeSelfDepositInputRecord(
            new BigDecimal("500.00")
        );

        doNothing().when(accountService).makeSelfDeposit(any(), any());

        mockMvc.perform(post(AccountRoutes.DEPOSIT.getPath())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositInput)))
                .andExpect(status().isNoContent());

        verify(accountService).makeSelfDeposit(any(), any());
    }

}

