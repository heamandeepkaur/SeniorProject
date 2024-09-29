package com.example.SeniorProject;

import com.example.SeniorProject.Model.Account;

public class LoginResponse
{
	private Account authenticatedAccount;
	private String token;
	private long expiresIn;

	public LoginResponse(Account authenticatedAccount, String token, long expiresIn)
	{
		this.authenticatedAccount = authenticatedAccount;
		this.token = token;
		this.expiresIn = expiresIn;
	}

	public String getToken()
    {
        return token;
    }

    public LoginResponse setToken(String token)
    {
        this.token = token;
        return this;
    }

    public long getExpiresIn()
    {
        return expiresIn;
    }

    public LoginResponse setExpiresIn(long expiresIn)
    {
        this.expiresIn = expiresIn;
        return this;
    }

    public Account getAuthenticateAccount()
    {
        return authenticatedAccount;
    }

    public void setAuthenticateAccount(Account authenticateAccount)
    {
        this.authenticatedAccount = authenticateAccount;
    }

	@Override
    public String toString()
    {
        return "LoginResponse{" +
            "token='" + token + '\'' +
            ", expiresIn=" + expiresIn +
            '}';
    }
}