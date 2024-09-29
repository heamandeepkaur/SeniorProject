package com.example.SeniorProject.Service;

import com.example.SeniorProject.DTOs.LoginUserDTO;
import com.example.SeniorProject.DTOs.RegisterUserDTO;
import com.example.SeniorProject.Exception.BadRequestException;
import com.example.SeniorProject.Model.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService
{
	private final CustomerRepository customerRepository;
	private final AccountRepository accountRepository;
        private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;

	public AuthenticationService(CustomerRepository customerRepository, AccountRepository accountRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager)
	{
		this.customerRepository = customerRepository;
		this.accountRepository = accountRepository;
                this.roleRepository = roleRepository;
                this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
	}

	public void signUp(RegisterUserDTO input)
  {
      if (accountRepository.findAccountByEmail(input.getEmail()) != null )
      {
          throw new BadRequestException("AAn account associated with this email already exists.");
      }
		  if(customerRepository.findAccountByCustomerName(input.getFirstName(), input.getLastName()) != null)
      {
          throw new BadRequestException("An account associated with your name already exists. Please log in instead. If you are trying to create a new account, please delete the current one first.");
      }
                Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);
                if (accountRepository.findAccountByEmail(input.getEmail()) != null )
                {
                    throw new BadRequestException("Account associated by this email already exists");
                }
                        if(customerRepository.findAccountByCustomerName(input.getFirstName(), input.getLastName()) != null)
                {
                    throw new BadRequestException("An account that is associate with your name already existed. please login instead. if you are trying to recreate n account please delete for current account first.");
                }
                Customer customer = new Customer(input.getFirstName(), input.getLastName(), input.getEmail(), input.getPhoneNumber(),"","","","");
                Account account = new Account(input.getEmail(), passwordEncoder.encode(input.getPassword()), false);
                if (!optionalRole.isEmpty()) {
                        account.setRole(optionalRole.get());
                }
                accountRepository.save(account);
                customer.setAccount(account);
                customerRepository.save(customer);
      }

        public Account authenticate(LoginUserDTO input)
        {
                Account account = accountRepository.findAccountByEmail(input.getEmail());

                if (account.getIsLock())
                {
                        throw new LockedException("Your account is locked due to multiple failed login attempts. Please reset your password.");
                }

                try
                {
                        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword()));
                        // Reset failed attempts on successful login
                        account.setFailedLoginAttempt(0);
                        accountRepository.save(account);
                        return account;
                }
                catch(BadCredentialsException e)
                {
                        // Increment failed attempts
                        int attempts = account.getFailedLoginAttempt() + 1;
                        account.setFailedLoginAttempt(attempts);

                        // Lock the account if attempts exceed 3
                if (attempts >= 3)
                {
                        account.setIsLocked(true);
                        accountRepository.save(account);
                        throw new LockedException("Your account is locked due to multiple failed login attempts. Please reset your password.");
                }
                accountRepository.save(account);
                throw new BadRequestException("Invalid credentials provided. Attempt " + attempts + " of 3.");
        }
    }
}