package com.example.shimpyo.domain.user.utils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Qualifier("2")
    private final RedisTemplate<String, Object> redisTokenTemplate;
    @Qualifier("3")
    private final RedisTemplate<String, Object> redisBlackListTemplate;

    public RedisService(@Qualifier("2") RedisTemplate<String, Object> redisTokenTemplate,
                        @Qualifier("3") RedisTemplate<String, Object> redisBlackListTemplate) {
        this.redisTokenTemplate = redisTokenTemplate;
        this.redisBlackListTemplate = redisBlackListTemplate;
    }


    // [#MOO5] 레디스 refresh-token 저장 로직
    public void saveRefreshToken(String userId, String refreshToken) {
        redisTokenTemplate.opsForValue().set("refresh_token" + userId, refreshToken, 2592000000L, TimeUnit.MILLISECONDS);
    }
    // [#MOO5] 레디스 refresh-token 저장 로직

    // [#MOO6] 레디스 refresh-token 재발급 로직
    // refreshToken 가져오는 로직
    public String getRefreshToken(String userId) {
        return Objects.requireNonNull(redisTokenTemplate.opsForValue().get("refresh_token" + userId)).toString();
    }
    // refresh Token 삭제 로직
    public void deleteRefreshToken(String userId) {
        redisTokenTemplate.delete("refresh_token" + userId);
    }
    // [#MOO6] 레디스 refresh-token 재발급 로직

    public void tokenBlackList(String token, long expired){
        redisBlackListTemplate.opsForValue().set("tokenBlackList" + token, "logout", expired, TimeUnit.MILLISECONDS);
    }

    public boolean isBlackList(String token){
        return redisBlackListTemplate.hasKey("tokenBlackList:" + token);
    }
}
