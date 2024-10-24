package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import com.example.SeniorProject.Service.SecretsManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.*;

import java.util.Optional;



@RestController
@RequestMapping("/account")
public class AccountController
{
    @Autowired
    private AccountService accountService;

    // Existing method to get account by id
    @GetMapping("/getAccount/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAccountById(@PathVariable int id)
    {
        try
        {
            Account account = accountService.getAccountById(id);
            return ResponseEntity.status(HttpStatus.OK).body(account);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    // Existing method to delete an account
    @DeleteMapping("/deleteAccount/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteAccount(@PathVariable int id)
    {
        accountService.deleteAccount(id);
        return ResponseEntity.status(HttpStatus.OK).body("your account has been successfully deleted.");
    }

    // Existing method to turn a user into an admin
    @PostMapping("/turnAdmin/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> turnAdmin(@PathVariable int id, @RequestParam String apiKey)
    {
        try
        {
            accountService.turnAdmin(id, apiKey);
            return ResponseEntity.status(HttpStatus.OK).body("you have successfully converted this account to admin.");
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @GetMapping("/getAccountInfo")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token)
    {
        try
        {
            Account account = accountService.getUserInfo(token);
            return ResponseEntity.status(HttpStatus.OK).body(account);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }
}