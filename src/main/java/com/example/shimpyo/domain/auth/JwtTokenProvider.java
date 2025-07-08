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

    public String createAccessToken(String id){
        return createToken(id, expiration, "sec");
    }

    public String createRefreshToken(String id){
        return createToken(id, expirationRT, "ref");
    }

    private String createToken(String id, long expiry, String type){
        Claims claims = Jwts.claims().setSubject(id);
        Key key = null;
        if(type.equals("ref")) {
           key = Keys.hmacShaKeyFor(secretKeyRT.getBytes());
        } else{
            key = Keys.hmacShaKeyFor(secretKey.getBytes());
        }
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiry) )
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(secretKeyRT.getBytes()).build().parseClaimsJws(token);
            return true;
        }catch (JwtException e){
            return false;
        }
    }

    public String getUserId(String token){
        return Jwts.parserBuilder().setSigningKey(secretKey.getBytes()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String getUserIdToRefresh(String token){
        return Jwts.parserBuilder().setSigningKey(secretKeyRT.getBytes()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
}
