package com.example.SeniorProject.DTOs;

public class RegisterUserDTO
{
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;

    public RegisterUserDTO(String email, String password, String firstName, String lastName, String phone)
    {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getPhoneNumber()
    {
        return phone;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phone = phone;
    }
}
