package com.example.shimpyo.domain.auth.service;

import com.example.shimpyo.domain.survey.dto.CourseResponseDto;
import com.example.shimpyo.domain.survey.dto.SuggestionRedisDto;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.CommonException;
import com.example.shimpyo.global.exceptionType.CourseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Qualifier("0")
    private final RedisTemplate<String, Object> redisSuggestionTemplate;
    private final ObjectMapper objectMapper;
    private static final String SUGGESTION_KEY_PREFIX = "suggestion:";

    public RedisService(@Qualifier("2") RedisTemplate<String, Object> redisTokenTemplate,
                        @Qualifier("3") RedisTemplate<String, Object> redisBlackListTemplate,
                        @Qualifier("0") RedisTemplate<String, Object> redisSuggestionTemplate,
                        ObjectMapper objectMapper) {
        this.redisTokenTemplate = redisTokenTemplate;
        this.redisBlackListTemplate = redisBlackListTemplate;
        this.redisSuggestionTemplate = redisSuggestionTemplate;
        this.objectMapper = objectMapper;
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
        return redisBlackListTemplate.hasKey("tokenBlackList" + token);
    }

    // Suggestion 을 User 정보와 함께 저장
    public void saveSuggestion(CourseResponseDto dto, String token, Long userId) {
        String key = SUGGESTION_KEY_PREFIX + token;

        SuggestionRedisDto redisDto = SuggestionRedisDto.builder()
                .userId(userId)
                .course(dto)
                .build();
        try {
            String value = objectMapper.writeValueAsString(redisDto);
            redisSuggestionTemplate.opsForValue().set(key, value, 24, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 저장 실패", e);
        }
    }

    public SuggestionRedisDto findSuggestionByTempId(String token) {
        String key = SUGGESTION_KEY_PREFIX + token;
        String value = (String) redisSuggestionTemplate.opsForValue().get(key);

        if (value == null) {
            throw new BaseException(CourseException.COURSE_NOT_FOUND);
        }

        try {
            return objectMapper.readValue(value, SuggestionRedisDto.class);
        } catch (JsonProcessingException e) {
            throw new BaseException(CommonException.SERVER_ERROR);
        }
    }

    public void deleteSuggestionByTempId(String tempId) {
        String key = SUGGESTION_KEY_PREFIX + tempId;

        Boolean exists = redisSuggestionTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            redisSuggestionTemplate.delete(key);
        } else {
            throw new BaseException(CourseException.EXPIRED_COURSE);
        }
    }

    public boolean existsSuggestionByToken(String token) {
        String key = SUGGESTION_KEY_PREFIX + token;
        Boolean exists = redisSuggestionTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists); // null 체크 포함
    }
}
