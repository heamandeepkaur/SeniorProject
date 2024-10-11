package com.example.SeniorProject.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

@Service
public class JwtService
{
	@Value("${security.jwt.secret-key}")
    private String secretKey;
    @Value("${security.jwt.expiration-time}")
    private Long expirationTime;

	public String extractUsername(String token)
	{
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
	{
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

	public String generateToken(UserDetails userDetails)
	{
		return generateToken(new HashMap<>(), userDetails);
	}

	public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails)
	{
        return buildToken(extraClaims, userDetails, expirationTime);
    }


	public long getExpirationTime()
	{
        return expirationTime;
	}

    public boolean isTokenValid(String token, UserDetails userDetails)
	{
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

	private String buildToken(Map<String, Object> extraClaims,UserDetails userDetails, long expiration)
	{
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

	private boolean isTokenExpired(String token)
	{
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token)
	{
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token)
	{
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getRemainingValidTime(String token)
    {
        Date expiration = extractExpiration(token);
        Date now = new Date();
        if (expiration.before(now)) {
            return 0;  // Token has already expired
        }

        // Calculate remaining time in milliseconds and convert to minutes, seconds, etc.
        long remainingMillis = expiration.getTime() - now.getTime();
        return TimeUnit.MILLISECONDS.toSeconds(remainingMillis);  // Return remaining time in seconds
    }

    private Key getSignInKey()
	{
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}