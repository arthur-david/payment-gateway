package br.com.nimblebaas.payment_gateway.controllers.authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.nimblebaas.payment_gateway.configs.security.SecurityConfig;
import br.com.nimblebaas.payment_gateway.dtos.input.authentication.LoginRequestRecord;
import br.com.nimblebaas.payment_gateway.dtos.input.authentication.RefreshTokenRequestRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.authentication.LoginResponseRecord;
import br.com.nimblebaas.payment_gateway.filters.authentication.AuthenticationFilter;
import br.com.nimblebaas.payment_gateway.services.authentication.AuthenticationService;

@WebMvcTest(
    controllers = AuthenticationController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, AuthenticationFilter.class}
    )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        LoginRequestRecord loginRequest = new LoginRequestRecord(
            "12345678900",
            "password123"
        );

        LoginResponseRecord loginResponse = new LoginResponseRecord(
            "access-token-123",
            "refresh-token-123",
            3600L
        );

        when(authenticationService.login(any(LoginRequestRecord.class))).thenReturn(loginResponse);

        mockMvc.perform(post(AuthenticationRoutes.LOGIN.getPath())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"))
                .andExpect(jsonPath("$.expiresIn").value(3600L));

        verify(authenticationService).login(any(LoginRequestRecord.class));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewToken() throws Exception {
        RefreshTokenRequestRecord refreshRequest = new RefreshTokenRequestRecord(
            "valid-refresh-token"
        );

        LoginResponseRecord loginResponse = new LoginResponseRecord(
            "new-access-token-123",
            "new-refresh-token-123",
            3600L
        );

        when(authenticationService.refreshToken(any(String.class))).thenReturn(loginResponse);

        mockMvc.perform(post(AuthenticationRoutes.REFRESH_TOKEN.getPath())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token-123"))
                .andExpect(jsonPath("$.expiresIn").value(3600L));

        verify(authenticationService).refreshToken(any(String.class));
    }
}

