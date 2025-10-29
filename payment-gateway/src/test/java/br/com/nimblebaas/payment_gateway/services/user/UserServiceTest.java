package br.com.nimblebaas.payment_gateway.services.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.nimblebaas.payment_gateway.configs.authentication.UserAuthenticated;
import br.com.nimblebaas.payment_gateway.dtos.input.user.ChangePasswordInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.input.user.UserInputRecord;
import br.com.nimblebaas.payment_gateway.dtos.output.user.UserOutputRecord;
import br.com.nimblebaas.payment_gateway.entities.user.User;
import br.com.nimblebaas.payment_gateway.enums.exception.BusinessRules;
import br.com.nimblebaas.payment_gateway.events.authentication.PasswordChangeEvent;
import br.com.nimblebaas.payment_gateway.exceptions.BusinessRuleException;
import br.com.nimblebaas.payment_gateway.repositories.user.UserRepository;
import br.com.nimblebaas.payment_gateway.services.account.AccountService;
import br.com.nimblebaas.payment_gateway.services.authentication.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCreationValidator userCreationValidator;

    @Mock
    private AccountService accountService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private UserService userService;

    private UserInputRecord userInput;
    private User user;
    private UserAuthenticated userAuthenticated;

    @BeforeEach
    void setUp() {
        userInput = new UserInputRecord(
            "John Doe",
            "12345678900",
            "john@example.com",
            "StrongPass123!"
        );

        user = new User();
        user.setName("John Doe");
        user.setCpf("12345678900");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");

        userAuthenticated = new UserAuthenticated(user);
    }

    @Test
    void create_WithValidData_ShouldCreateUserAndReturnOutputRecord() {
        doNothing().when(userCreationValidator).validate(any(UserInputRecord.class));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(accountService).openAccount(any(User.class));

        UserOutputRecord result = userService.create(userInput);

        assertNotNull(result);
        assertEquals("John Doe", result.name());
        assertEquals("12345678900", result.cpf());
        assertEquals("john@example.com", result.email());
        verify(userCreationValidator).validate(userInput);
        verify(passwordEncoder).encode("StrongPass123!");
        verify(userRepository).save(any(User.class));
        verify(accountService).openAccount(any(User.class));
    }

    @Test
    void changePassword_WithValidData_ShouldChangePasswordSuccessfully() {
        ChangePasswordInputRecord changePasswordInput = new ChangePasswordInputRecord(
            "StrongPass123!",
            "NewStrongPass456!"
        );

        when(userRepository.findByCpf(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        doNothing().when(userCreationValidator).validateIfPasswordIsStrong(anyString());
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        doNothing().when(refreshTokenService).revokeUserRefreshTokens(any(User.class));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader(any())).thenReturn("Mozilla/5.0");

        userService.changePassword(userAuthenticated, changePasswordInput);

        verify(userCreationValidator).validateIfPasswordIsStrong("NewStrongPass456!");
        verify(refreshTokenService).revokeUserRefreshTokens(user);
        verify(userRepository).save(user);
        verify(eventPublisher).publishEvent(any(PasswordChangeEvent.class));
        assertNotNull(user.getLastChangedPasswordAt());
    }

    @Test
    void changePassword_WithInvalidCurrentPassword_ShouldThrowException() {
        ChangePasswordInputRecord changePasswordInput = new ChangePasswordInputRecord(
            "WrongPassword123!",
            "NewStrongPass456!"
        );

        when(userRepository.findByCpf(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> userService.changePassword(userAuthenticated, changePasswordInput)
        );

        assertEquals(BusinessRules.INVALID_PASSWORD.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void getUser_WithValidCpf_ShouldReturnUserOutputRecord() {
        when(userRepository.findByCpfOrEmail(anyString(), any())).thenReturn(Optional.of(user));

        UserOutputRecord result = userService.getUser("12345678900");

        assertNotNull(result);
        assertEquals("John Doe", result.name());
        assertEquals("12345678900", result.cpf());
        assertEquals("john@example.com", result.email());
    }

    @Test
    void getUser_WithValidEmail_ShouldReturnUserOutputRecord() {
        when(userRepository.findByCpfOrEmail(any(), anyString())).thenReturn(Optional.of(user));

        UserOutputRecord result = userService.getUser("john@example.com");

        assertNotNull(result);
        assertEquals("John Doe", result.name());
        assertEquals("12345678900", result.cpf());
        assertEquals("john@example.com", result.email());
    }

    @Test
    void getUser_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.findByCpfOrEmail(any(), any())).thenReturn(Optional.empty());

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> userService.getUser("99999999999")
        );

        assertEquals(BusinessRules.USER_NOT_FOUND.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void findByCpfOrEmail_WithCpf_ShouldReturnUser() {
        when(userRepository.findByCpfOrEmail(anyString(), any())).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByCpfOrEmail("12345678900");

        assertTrue(result.isPresent());
        assertEquals("12345678900", result.get().getCpf());
    }

    @Test
    void findByCpfOrEmail_WithEmail_ShouldReturnUser() {
        when(userRepository.findByCpfOrEmail(any(), anyString())).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByCpfOrEmail("john@example.com");

        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get().getEmail());
    }

    @Test
    void getUserByCpf_WithValidCpf_ShouldReturnUser() {
        when(userRepository.findByCpf(anyString())).thenReturn(Optional.of(user));

        User result = userService.getUserByCpf("12345678900");

        assertNotNull(result);
        assertEquals("12345678900", result.getCpf());
    }

    @Test
    void getUserByCpf_WithInvalidCpf_ShouldThrowException() {
        when(userRepository.findByCpf(anyString())).thenReturn(Optional.empty());

        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> userService.getUserByCpf("99999999999")
        );

        assertEquals(BusinessRules.USER_NOT_FOUND.name(), exception.getErrorDTO().getReason());
    }

    @Test
    void findByCpf_WithValidCpf_ShouldReturnOptionalUser() {
        when(userRepository.findByCpf(anyString())).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByCpf("12345678900");

        assertTrue(result.isPresent());
        assertEquals("12345678900", result.get().getCpf());
    }

    @Test
    void findByCpf_WithInvalidCpf_ShouldReturnEmptyOptional() {
        when(userRepository.findByCpf(anyString())).thenReturn(Optional.empty());

        Optional<User> result = userService.findByCpf("99999999999");

        assertTrue(result.isEmpty());
    }
}

