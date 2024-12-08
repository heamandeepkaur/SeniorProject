package com.example.SeniorProject.Service;

import com.example.SeniorProject.Model.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.server.*;

import java.util.*;

@Service
public class AccountService
{
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private SecretsManagerService secretsManagerService;


    // Existing method to get account by id
    public Account getAccountById(int id)
    {
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        return account;
    }

    // Existing method to delete an account
    public void deleteAccount(int id)
    {
        accountRepository.deleteById(id);
    }

    // Existing method to turn a user into an admin
    public void turnAdmin(int id, String apiKey)
    {
        String key = secretsManagerService.getSecretValue("adminAccountKey");
        if(key == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No API key found in AWS Secret Manager.");
        }
        if(!key.equals(apiKey))
        {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "API key does not match.");
        }

        Account account = accountRepository.getAccountById(id).get(0);
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.ADMIN);
        if (optionalRole.isPresent())
        {
            account.setRole(optionalRole.get());
        }
        accountRepository.save(account);
    }

    public Account getUserInfo(String token)
    {
        String jwtToken = token.replace("Bearer ", "");

        // Validate the token and extract username
        if (jwtService.isTokenExpired(jwtToken))
        {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your session has expired, please log in again");
        }

        String username = jwtService.extractUsername(jwtToken);
        Account account = accountRepository.findAccountByEmail(username);

        if (account == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found with email " + username);
        }

        return account;
    }

    public void deleteAllUnverifiedAccounts()
    {
        List<Account> accounts = accountRepository.findUnverifiedAccounts();
        if (accounts.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No unverified accounts found");
        }
        try
        {
            accountRepository.deleteAll(accounts);
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
