package com.example.SeniorProject.Service;

import com.example.SeniorProject.Model.*;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private SecretsManagerService secretsManagerService;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test for getAccountById
    @Test
    void getAccountByIdAccountExists() {
        int accountId = 1;
        Account mockAccount = new Account();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        Account result = accountService.getAccountById(accountId);

        assertNotNull(result);
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void getAccountByIdAccountNotFound() {
        int accountId = 1;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountService.getAccountById(accountId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
        verify(accountRepository, times(1)).findById(accountId);
    }

    // Test for deleteAccount
    @Test
    void deleteAccountValidId() {
        int accountId = 1;

        doNothing().when(accountRepository).deleteById(accountId);

        accountService.deleteAccount(accountId);

        verify(accountRepository, times(1)).deleteById(accountId);
    }

    // Test for turnAdmin
    @Test
    void turnAdminValidApiKeyAndAccountExists() {
        int accountId = 1;
        String validApiKey = "validApiKey";
        Account mockAccount = new Account();
        Role adminRole = new Role();
        adminRole.setName(RoleEnum.ADMIN);

        when(secretsManagerService.getSecretValue("adminAccountKey")).thenReturn(validApiKey);
        when(accountRepository.getAccountById(accountId)).thenReturn(List.of(mockAccount));
        when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.of(adminRole));

        accountService.turnAdmin(accountId, validApiKey);

        assertEquals(adminRole, mockAccount.getRole());
        verify(accountRepository, times(1)).save(mockAccount);
    }

    @Test
    void turnAdminInvalidApiKey() {
        int accountId = 1;
        String invalidApiKey = "invalidApiKey";
        String storedApiKey = "storedApiKey";

        when(secretsManagerService.getSecretValue("adminAccountKey")).thenReturn(storedApiKey);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountService.turnAdmin(accountId, invalidApiKey));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("API key does not match.", exception.getReason());
    }

    @Test
    void turnAdminNoApiKeyInSecretsManager() {
        int accountId = 1;

        when(secretsManagerService.getSecretValue("adminAccountKey")).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountService.turnAdmin(accountId, "anyApiKey"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("No API key found in AWS Secret Manager.", exception.getReason());
    }

    // Test for getUserInfo
    @Test
    void getUserInfoValidTokenAndAccountExists() {
        String token = "Bearer validToken";
        String jwtToken = "validToken";
        String username = "test@example.com";
        Account mockAccount = new Account();

        when(jwtService.isTokenExpired(jwtToken)).thenReturn(false);
        when(jwtService.extractUsername(jwtToken)).thenReturn(username);
        when(accountRepository.findAccountByEmail(username)).thenReturn(mockAccount);

        Account result = accountService.getUserInfo(token);

        assertNotNull(result);
        verify(accountRepository, times(1)).findAccountByEmail(username);
    }

    @Test
    void getUserInfoExpiredToken() {
        String token = "Bearer expiredToken";
        String jwtToken = "expiredToken";

        when(jwtService.isTokenExpired(jwtToken)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountService.getUserInfo(token));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Your session has expired, please log in again", exception.getReason());
    }

    @Test
    void getUserInfoAccountNotFound() {
        String token = "Bearer validToken";
        String jwtToken = "validToken";
        String username = "test@example.com";

        when(jwtService.isTokenExpired(jwtToken)).thenReturn(false);
        when(jwtService.extractUsername(jwtToken)).thenReturn(username);
        when(accountRepository.findAccountByEmail(username)).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountService.getUserInfo(token));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("No account found with email " + username, exception.getReason());
    }

    @Test
    void deleteAllUnverifiedAccountsWithUnverifiedAccounts()
    {
            Account account1 = new Account();
            Account account2 = new Account();
            Account account3 = new Account();

        // Arrange
        List<Account> unverifiedAccounts = List.of(new Account()); // mock unverified accounts
        when(accountRepository.findUnverifiedAccounts()).thenReturn(unverifiedAccounts);

        // Act
        accountService.deleteAllUnverifiedAccounts();

        // Assert
        verify(accountRepository, times(1)).deleteAll(unverifiedAccounts);
    }

    @Test
    void deleteAllUnverifiedAccountsNoUnverifiedAccounts() {
        // Arrange
        when(accountRepository.findUnverifiedAccounts()).thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            accountService.deleteAllUnverifiedAccounts();
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("No unverified accounts found", exception.getReason());
    }

    @Test
    void deleteAllUnverifiedAccountsDeleteThrowsException() {
        // Arrange
        List<Account> unverifiedAccounts = List.of(new Account());
        when(accountRepository.findUnverifiedAccounts()).thenReturn(unverifiedAccounts);
        doThrow(new RuntimeException("Database error")).when(accountRepository).deleteAll(unverifiedAccounts);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            accountService.deleteAllUnverifiedAccounts();
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Database error", exception.getReason());
    }
}
