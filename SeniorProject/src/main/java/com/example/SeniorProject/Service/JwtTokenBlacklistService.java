package com.example.SeniorProject.Service;

import org.springframework.data.redis.core.*;
import org.springframework.stereotype.*;

import java.util.*;
import java.util.concurrent.*;

@Service
public class JwtTokenBlacklistService
{
    private final RedisTemplate<String, String> redisTemplate;

    public JwtTokenBlacklistService(RedisTemplate<String, String> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long expirationTimeInSeconds)
    {
        redisTemplate.opsForValue().set(token, "blacklisted", expirationTimeInSeconds, TimeUnit.SECONDS);
    }

    public boolean isTokenBlacklisted(String token)
    {
        if (token == null)
        {
            throw new IllegalArgumentException("JWT token is null");
        }
        return redisTemplate.hasKey(token);
    }

    public void addTokenForUser(String username, String token, long expirationTimeInSeconds)
    {
        redisTemplate.opsForSet().add(username, token);
        redisTemplate.expire(username, expirationTimeInSeconds, TimeUnit.SECONDS);
    }

    public void invalidateTokensForUser(String username)
    {
        Set<String> tokens = redisTemplate.opsForSet().members(username);
        if(tokens != null)
        {
            for (String token : tokens)
            {
                blacklistToken(token, redisTemplate.getExpire(String.valueOf(token), TimeUnit.SECONDS));
            }
            redisTemplate.delete(username);
        }
    }
}