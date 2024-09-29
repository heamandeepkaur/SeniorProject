package com.example.SeniorProject.Model;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.*;
import java.util.Collection;
import java.util.List;


@Entity
@Table(name = "account")
public class Account implements UserDetails
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private int id;
    @NotNull
    @Column(name = "email")
    private String email;
    @NotNull
    @Size(min = 8, message = "password must be at least 8 character long")
    @Column(name = "password")
    private String password;
    @NotNull
    @Column(name = "is_admin")
    private boolean isAdmin;
    @NotNull
    @Column(name = "is_verified")
    private boolean isVerified;
    @NotNull
    @Column(name = "failed_attempt")
    private int failedLoginAttempt;
    @NotNull
    @Column(name = "account_locked")
    private boolean isLocked;

    @OneToOne(mappedBy = "account")
    @JsonBackReference
    private Customer customer;

    public Account()
    {

    }

    public Account(String email, String password, boolean isAdmin)
    {
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.failedLoginAttempt = 0;
        this.isLocked = false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return List.of();
    }


    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Customer getCustomer()
    {
        return customer;
    }

    public boolean isAdmin()
    {
        return isAdmin;
    }

    public void setAdmin(boolean admin)
    {
        isAdmin = admin;
    }

    public String getPassword()
    {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public int getId()
    {
        return id;
    }

    public int getFailedLoginAttempt()
    {
        return failedLoginAttempt;
    }

    public void setFailedLoginAttempt(int failedLoginAttempt)
    {
        this.failedLoginAttempt = failedLoginAttempt;
    }

    public boolean getIsLock()
    {
        return isLocked;
    }

    public void setIsLocked(boolean isLocked)
    {
        this.isLocked = isLocked;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
}
