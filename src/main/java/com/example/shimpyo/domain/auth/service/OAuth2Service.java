package com.example.shimpyo.domain.auth.service;

import com.example.shimpyo.domain.auth.JwtTokenProvider;
import com.example.shimpyo.domain.auth.dto.LoginResponseDto;
import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.auth.repository.UserAuthRepository;
import com.example.shimpyo.domain.user.entity.SocialType;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.user.repository.UserRepository;
import com.example.shimpyo.domain.user.utils.RedisService;
import com.example.shimpyo.domain.utils.NicknamePrefixLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuth2Service {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    @Value("${spring.security.oauth2.client.provider.kakao.admin-key}")
    private String KAKAO_ADMIN_KEY;
    @Value("${jwt.expirationRT}")
    long expirationRT;

    @Transactional
    public LoginResponseDto kakaoLogin(String accessToken, HttpServletResponse responseCookie) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );
        // responseBody에 있는 정보 꺼내기
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        String email = jsonNode.get("kakao_account").get("email").asText();
        String id = jsonNode.get("id").asText();

        // find로 가져올 데이터가 있으면 기존 회원, 아니면 신규 가입
        Optional<UserAuth> optionalUserAuth = userAuthRepository.findByUserLoginIdAndSocialType(email, SocialType.KAKAO);

        UserAuth user;
        if(optionalUserAuth.isPresent()){
            user = optionalUserAuth.get();
            createToken(email, user.getId(), responseCookie);
        }else{
            User newUser = userRepository.save(User.builder()
                    .email(email)
                    .nickname(NicknamePrefixLoader.generateNickNames())
                    .build());

            userAuthRepository.save(UserAuth.builder()
                    .user(newUser)
                    .userLoginId(email)
                    .password(null)
                    .oauthId(id)
                    .socialType(SocialType.KAKAO)
                    .build());

            return null;
        }

        return LoginResponseDto.toDto(user);
    }

    // 소셜 로그인 시 토큰 생성
    private void createToken(String loginId, long id, HttpServletResponse response){
        String accessToken = jwtTokenProvider.createAccessToken(loginId, id);
        String refreshToken = jwtTokenProvider.createRefreshToken(loginId, id, false);

        redisService.saveRefreshToken(loginId, refreshToken);

        ResponseCookie accessCookie = createCookie("access_token", accessToken, 1800);
        ResponseCookie refreshCookie = createCookie("refresh_token", refreshToken, expirationRT / 1000L);

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

    }

    private ResponseCookie createCookie(String name, String value, long maxAgeSeconds){
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) // 운영 환경에서는 true 권장
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }

    public void unlinkKaKao(UserAuth userAuth) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + KAKAO_ADMIN_KEY);

        RestTemplate restTemplate = new RestTemplate();

        // POST 요청으로 데이터 전송
        // HttpHeaders 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("target_id_type", "user_id");  // target_id_type
        params.add("target_id", userAuth.getOauthId());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<String> exchange = restTemplate.exchange(
                "https://kapi.kakao.com/v1/user/unlink",  // 요청할 URL
                HttpMethod.POST,                 // HTTP 메서드
                entity,
                String.class                     // 응답 타입
        );
        System.out.println("success : " + exchange.getBody());
    }
}
