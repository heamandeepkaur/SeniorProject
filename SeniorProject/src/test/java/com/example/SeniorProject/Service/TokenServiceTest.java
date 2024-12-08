package com.example.SeniorProject.Service;

import com.example.SeniorProject.Model.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TokenServiceTest
{
    private TokenService tokenService;

    @BeforeEach
    public void setUp()
    {
        tokenService = new TokenService();
    }

    @Test
    public void testGenerateTokenCreatesValidToken()
    {
        // Step 1: Call generateToken with a sample email
        String email = "test@example.com";
        String token = tokenService.generateToken(email);

        // Step 2: Assert that the returned token is not null
        assertNotNull(token, "Token should not be null");

        // Step 3: Assert that the token is stored in the tokenStore and linked to the correct TokenData
        TokenData tokenData = tokenService.getTokenStore().get(token);
        assertNotNull(tokenData, "TokenData should be present in the tokenStore");

        // Step 4: Ensure TokenData has the correct email, timestamp, and isUsed is false
        assertEquals(email, tokenData.getEmail(), "TokenData should have the correct email");
        assertFalse(tokenData.isUsed(), "TokenData should not be marked as used");

        // Step 5: Ensure the timestamp is within an acceptable range (1 second tolerance)
        long currentTime = System.currentTimeMillis();
        assertTrue(currentTime - tokenData.getTimestamp() < 1000, "Timestamp should be recent");

        // Optionally, verify that the token is a properly encoded Base64 string
        assertDoesNotThrow(() -> Base64.getDecoder().decode(token), "Token should be a valid Base64 string");
    }

    @Test
    public void testValidateTokenWithValidTokenAndReturnsEmail()
    {
        String email = "test@example.com";
        String token = tokenService.generateToken(email);
        String returnedEmail = tokenService.validateToken(token);

        assertNotNull(returnedEmail, "Email should not be null");
        assertEquals(email, returnedEmail, "Email should be the same");
    }

    @Test
    public void testValidateTokenWithExpiredTokenAndReturnsNull()
    {
        // Step 1: Manually create a token with an expired timestamp
        String email = "expired@example.com";
        String token = tokenService.generateToken(email);

        // Simulate an expired token (30 minutes expiration time, set to 1 hour ago)
        long expiredTimestamp = System.currentTimeMillis() - (60 * 60 * 1000); // 1 hour ago
        TokenData expiredTokenData = new TokenData(email, expiredTimestamp, false);

        // Replace the current TokenData with the expired one in the tokenStore
        tokenService.getTokenStore().put(token, expiredTokenData);
        String returnedEmail = tokenService.validateToken(token);
        assertNull(returnedEmail, "The validateToken method should return null for expired tokens");
        assertFalse(tokenService.getTokenStore().containsKey(token), "Expired tokens should be removed from the tokenStore");
    }

    @Test
    public void testValidTokenWithInvalidToke()
    {
        String token = "test123456";
        assertNull(tokenService.validateToken(token));
    }

    @Test
    public void testValidateTokenWithUsedToken()
    {
        String email = "test@example.com";
        String token = tokenService.generateToken(email);
        tokenService.markTokenAsUsed(token);
        assertNull(tokenService.validateToken(token));
        TokenData tokenData = tokenService.getTokenStore().get(token);
        assertNotNull(tokenData, "TokenData should be present in the tokenStore");
        assertEquals(email, tokenData.getEmail(), "TokenData should have the correct email");
        assertTrue(tokenData.isUsed(), "TokenData should be marked as used");
    }

    @Test
    public void testExpiredTokenRemovalFromTokenStore()
    {
        String email = "expired_removal@example.com";
        String token = tokenService.generateToken(email);
        long expiredTimestamp = System.currentTimeMillis() - (31 * 60 * 1000); // 31 minutes ago
        TokenData expiredTokenData = new TokenData(email, expiredTimestamp, false);

        tokenService.getTokenStore().put(token, expiredTokenData);
        String returnedEmail = tokenService.validateToken(token);
        assertNull(returnedEmail, "The validateToken method should return null for expired tokens.");
        assertFalse(tokenService.getTokenStore().containsKey(token), "Expired tokens should be removed from the tokenStore after validation.");
    }

    @Test
    public void testGenerateTokenCreateUniqueToken()
    {
        String email = "test@example.com";
        String token1 = tokenService.generateToken(email);
        String token2 = tokenService.generateToken(email);
        assertNotNull(token1, "Token should not be null");
        assertNotNull(token2, "Token should not be null");
        assertNotEquals(token1, token2);
    }

    @Test
    void testGenerateToken_withNullEmail_throwsException() {
        // Step 1: Call generateToken with a null email and expect an exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tokenService.generateToken(null);
        });

        // Step 2: Assert that the exception message is appropriate
        String expectedMessage = "Email cannot be null";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage), "Exception message should indicate that email cannot be null.");
    }
}