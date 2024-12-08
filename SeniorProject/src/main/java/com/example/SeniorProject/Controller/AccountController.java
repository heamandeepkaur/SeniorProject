package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.*;

import java.util.*;


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
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    // Existing method to delete an account
    @DeleteMapping("/deleteAccount/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteAccount(@PathVariable int id)
    {
        try
        {
            accountService.deleteAccount(id);
            return ResponseEntity.status(HttpStatus.OK).body("your account has been successfully deleted.");
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
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
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
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
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @DeleteMapping("/deleteUnverifiedAccounts")
    public ResponseEntity<?> deleteUnverifiedAccounts()
    {
        try
        {
            accountService.deleteAllUnverifiedAccounts();
            return ResponseEntity.status(HttpStatus.OK).body("All unverified accounts have been successfully deleted.");
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting unverified accounts.");
        }
    }

    @GetMapping("/customerId")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCustomerId(@RequestHeader("Authorization") String token)
    {
        try
        {
            Account account = accountService.getUserInfo(token);

            // Check if the account is found
            if (account == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
            }

            // Get the associated Customer
            Customer customer = account.getCustomer();

            // Check if the Customer is associated with the Account
            if (customer == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer ID not found");
            }

            // Return the Customer ID
            return ResponseEntity.ok(customer.getId());
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }
}