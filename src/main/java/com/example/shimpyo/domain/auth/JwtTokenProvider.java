package com.example.shimpyo.domain.auth;

import com.example.shimpyo.global.BaseException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static com.example.shimpyo.global.exceptionType.TokenException.INVALID_TOKEN;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    String secretKey;
    @Value("${jwt.secretKeyRT}")
    String secretKeyRT;
    @Value("${jwt.expiration}")
    long expiration;
    @Value("${jwt.expirationRT}")
    long expirationRT;

    public String createToken(String id){
        Claims claims = Jwts.claims().setSubject(id);
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+ expiration) )
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getMemberIdFromToken(String bearerToken){
        try{
            String token = bearerToken.replace("Bearer ", "");

            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.getSubject());
        }catch (Exception e){
            throw new BaseException(INVALID_TOKEN);
        }
    }
}
