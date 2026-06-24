package com.codewithmosh.store.services;

import com.codewithmosh.store.config.JwtConfig;
import com.codewithmosh.store.entities.Role;
import com.codewithmosh.store.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class JwtService {

    private final JwtConfig jwtConfig;


    public Jwt generateAccessToken(User user) {

        final long tokenExpiration = 300;

        return generateToken(user, jwtConfig.getAccessTokenExpiration());


    }

    public Jwt generateRefreshToken(User user) {

        final long tokenExpiration = 604800;

        return generateToken(user, jwtConfig.getRefreshTokenExpiration());

    }

    private Jwt generateToken(User user, long tokenExpiration) {
       var claims = Jwts.claims()
                .subject(user.getId().toString())
                .add("email", user.getEmail())
                .add("name", user.getName())
                .add("role", user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
                .build();

       return new Jwt(claims, jwtConfig.getSecretKey());
    }

    public boolean validateToken(String token) {
        try {
            var claims = getClaims(token);

            System.out.println("Token claims: " + claims);

           return claims.getExpiration().after(new Date());

        }
        catch (JwtException ex) {
            return false;
        }

    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Jwt parseToken(String token) {
        try {
            var claims = getClaims(token);
            return new Jwt(claims, jwtConfig.getSecretKey());
        } catch (JwtException ex) {
            return null;
        }
    }


}

