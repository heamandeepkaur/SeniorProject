package com.example.SeniorProject.Configuration;

import com.example.SeniorProject.Model.Account;
import com.example.SeniorProject.Model.AccountRepository;
import com.example.SeniorProject.Model.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class ApplicationConfiguration
{
	private final CustomerRepository customerRepository;
	private final AccountRepository accountRepository;

	public ApplicationConfiguration(CustomerRepository customerRepository, AccountRepository accountRepository)
	{
		this.customerRepository = customerRepository;
		this.accountRepository = accountRepository;
	}

	@Bean
	UserDetailsService userDetailsService()
    {
        return username ->
        {
            Account account = accountRepository.findAccountByEmail(username);
            if (account == null)
            {
                throw new UsernameNotFoundException("User not found");
            }
            return account;
        };
    }

	@Bean
	BCryptPasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder(10);
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
	{
		try
        {
            return configuration.getAuthenticationManager();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
	}

	@Bean
	AuthenticationProvider authenticationProvider()
    {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
}