package com.example.shimpyo.domain.user.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTokenTemplate;

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
}
