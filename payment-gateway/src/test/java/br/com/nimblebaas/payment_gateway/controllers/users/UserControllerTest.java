package br.com.nimblebaas.payment_gateway.controllers.users;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import br.com.nimblebaas.payment_gateway.dtos.input.user.ChangePasswordInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.input.user.UserInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.user.UserOutputRecord;
import br.com.nimblebaas.payment_gateway.filters.authentication.AuthenticationFilter;
import br.com.nimblebaas.payment_gateway.helpers.WithMockUserAuthenticated;
import br.com.nimblebaas.payment_gateway.services.user.UserService;

@WebMvcTest(
    controllers = UserController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, AuthenticationFilter.class}
    )
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void create_WithValidData_ShouldReturnCreated() throws Exception {
        UserInputRecord userInput = new UserInputRecord(
            "John Doe",
            "12345678900",
            "john@example.com",
            "password123"
        );

        UserOutputRecord userOutput = new UserOutputRecord(
            "John Doe",
            "12345678900",
            "john@example.com"
        );

        when(userService.create(any(UserInputRecord.class))).thenReturn(userOutput);

        mockMvc.perform(post(UserRoutes.REGISTER.getPath())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userInput)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService).create(any(UserInputRecord.class));
    }

    @Test
    @WithMockUser
    void changePassword_WithValidData_ShouldReturnNoContent() throws Exception {
        ChangePasswordInputRecord changePasswordInput = new ChangePasswordInputRecord(
            "oldPassword123",
            "newPassword123"
        );

        doNothing().when(userService).changePassword(any(), any());

        mockMvc.perform(patch(UserRoutes.CHANGE_PASSWORD.getPath())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordInput)))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(any(), any());
    }

    @Test
    void getUser_WithValidCpf_ShouldReturnUser() throws Exception{
        UserOutputRecord userOutput = new UserOutputRecord(
            "John Doe",
            "12345678900",
            "john@example.com"
        );

        when(userService.getUser(any(String.class))).thenReturn(userOutput);

        mockMvc.perform(get(UserRoutes.GET_USER.getPath())
                .param("cpfOrEmail", "12345678900")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService).getUser(any(String.class));
    }

    @Test
    void getUser_WithValidEmail_ShouldReturnUser() throws Exception {
        UserOutputRecord userOutput = new UserOutputRecord(
            "John Doe",
            "12345678900",
            "john@example.com"
        );

        when(userService.getUser(any(String.class))).thenReturn(userOutput);

        mockMvc.perform(get(UserRoutes.GET_USER.getPath())
                .param("cpfOrEmail", "john@example.com")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService).getUser(any(String.class));
    }

    @Test
    @WithMockUserAuthenticated(
        name = "John Doe",
        cpf = "12345678900",
        email = "john@example.com"
    )
    void getCurrentUser_ShouldReturnCurrentUser() throws Exception {
        mockMvc.perform(get(UserRoutes.GET_CURRENT_USER.getPath())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

}

