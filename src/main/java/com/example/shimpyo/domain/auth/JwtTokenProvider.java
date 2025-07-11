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
    // 자동 로그인 체크 시 유효시간 30일
    @Value("${jwt.expirationALRT}")
    long expirationALRT;
    // 자동 로그인 체크 X 시 유효시간 2시간.
    @Value("${jwt.expirationRT}")
    long expirationRT;

    public String createAccessToken(String id){
        return createToken(id, expiration, "sec");
    }

    // refresh token 재발급 로직
    public String createRefreshToken(String id, boolean isRememberMe){
        if(isRememberMe){
            // 자동 로그인 체크시 30일 refresh-token
            return createToken(id, expirationALRT, "ref");
        }else{
            // 자동 로그인 미체크시 2시간의 refresh-token
            return createToken(id, expirationRT, "ref");
        }
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
