package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;


@RestController
@RequestMapping("/account")
public class AccountController {

    
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/getAccount/{id}")
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody Iterable<Account>getAccountById(@PathVariable int id) {
        return accountRepository.getAccountById(id);
    }

    @DeleteMapping("/deleteAccount/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAccount(@PathVariable int id) {
        accountRepository.deleteById(id);
        return ResponseEntity.ok().build();
        
    }

    @PostMapping("/turnAdmin/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> turnAdmin(@PathVariable int id) {
        Account account = accountRepository.getAccountById(id).get(0);
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.ADMIN);
        if (!optionalRole.isEmpty()) {
            account.setRole(optionalRole.get());
        }
        accountRepository.save(account);
        return ResponseEntity.ok().build();
    }
}