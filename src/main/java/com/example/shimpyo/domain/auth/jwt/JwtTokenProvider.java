package com.example.shimpyo.domain.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;


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

    // accesstoken 생성
    public String createAccessToken(String loginId){
        return createToken(loginId, expiration, "access");
    }

    // refresh token 재발급 로직
    public String createRefreshToken(String loginId, boolean isRememberMe){
        long expiry = isRememberMe ? expirationALRT : expirationRT;
        return createToken(loginId, expiry, "refresh");
    }

    // JWT 생성 공통 메서드
    private String createToken(String loginId, long expiry, String type){
        Key key = null;
        if(type.equals("refresh")) {
           key = Keys.hmacShaKeyFor(secretKeyRT.getBytes());
        } else{
            key = Keys.hmacShaKeyFor(secretKey.getBytes());
        }
        return Jwts.builder()
                .claim("loginId", loginId)
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

    public String getUserNameFromRefresh(String token){
        return (String) Jwts.parserBuilder().setSigningKey(secretKeyRT.getBytes()).build()
                .parseClaimsJws(token).getBody().get("loginId");
    }
    public String getUserNameFromAccess(String token){
        return (String) Jwts.parserBuilder().setSigningKey(secretKey.getBytes()).build()
                .parseClaimsJws(token).getBody().get("loginId");
    }

    // JWT 남은 만료 시간을 추출하는 메서드
    public long getRemainingExpiration(String token){
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date expiration = claims.getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
