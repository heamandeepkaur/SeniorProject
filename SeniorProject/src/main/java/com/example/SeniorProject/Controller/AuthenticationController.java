package com.example.SeniorProject.Controller;

import com.example.SeniorProject.DTOs.LoginUserDTO;
import com.example.SeniorProject.DTOs.RegisterUserDTO;
import com.example.SeniorProject.Exception.BadRequestException;
import com.example.SeniorProject.LoginResponse;
import com.example.SeniorProject.Model.Account;
import com.example.SeniorProject.Service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
public class AuthenticationController
{
	private final JwtService jwtService;
	private final AuthenticationService authenticationService;
    private final JwtTokenBlacklistService jwtTokenBlacklistService;

	public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService, JwtTokenBlacklistService jwtTokenBlacklistService)
    {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.jwtTokenBlacklistService = jwtTokenBlacklistService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDTO registerUserDTO)
    {
        authenticationService.signUp(registerUserDTO);
        return ResponseEntity.ok("Your account has been successfully created. A verification email has been sent to the address provided. Please follow the instructions in the email to verify your account. Unverified accounts and associated profiles will be automatically deleted at 12:00 AM Pacific Time.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDTO loginUserDTO)
    {
        try
        {
            Account authenticatedUser = authenticationService.authenticate(loginUserDTO);
            String jwtToken = jwtService.generateToken(authenticatedUser);
            LoginResponse loginResponse = new LoginResponse(authenticatedUser,jwtToken, jwtService.getExpirationTime());
            jwtTokenBlacklistService.addTokenForUser(authenticatedUser.getUsername(), jwtToken, jwtService.getExpirationTimeInSeconds(jwtToken)); // Save the token for the user
            return ResponseEntity.ok(loginResponse);
        }
        catch (BadRequestException exception)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials provided.");
        }
        catch (LockedException exception)
        {
            return ResponseEntity.status(HttpStatus.LOCKED).body("your account is id locked");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader() String token)
    {
        if(token == null || !token.startsWith("Bearer "))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token format");
        }
        String jwtToken = token.substring(7);
        long expirationTime = jwtService.getExpirationTime();
        jwtTokenBlacklistService.blacklistToken(jwtToken, expirationTime);
        return ResponseEntity.status(HttpStatus.OK).body("Your account has been successfully logged out.");
    }

    @PostMapping("/invalidate")
    public ResponseEntity<?> invalidateTokens(@RequestBody String username)
    {
        jwtTokenBlacklistService.invalidateTokensForUser(username);
        return ResponseEntity.ok().build();
    }
}