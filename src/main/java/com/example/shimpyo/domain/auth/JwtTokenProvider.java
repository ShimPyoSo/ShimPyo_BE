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

    // accesstoken 생성
    public String createAccessToken(String loginId, long id){
        return createToken(loginId, id, expiration, "sec");
    }

    // refresh token 재발급 로직
    public String createRefreshToken(String loginId, long id, boolean isRememberMe){
        long expiry = isRememberMe ? expirationALRT : expirationRT;
        return createToken(loginId, id, expiry, "ref");
    }

    // JWT 생성 공통 메서드
    private String createToken(String loginId, long id, long expiry, String type){
        Claims claims = Jwts.claims().setSubject(loginId);
        claims.put("id", id);
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

    public String getUserNameToRefresh(String token){
        return Jwts.parserBuilder().setSigningKey(secretKeyRT.getBytes()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public long getUserIdToRefresh(String token){

        return (long) Jwts.parserBuilder().setSigningKey(secretKeyRT.getBytes()).build()
                .parseClaimsJws(token).getBody().get("id");
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
