package com.example.shimpyo.domain.auth.service;

import com.example.shimpyo.domain.auth.dto.LoginResponseDto;
import com.example.shimpyo.domain.user.entity.SocialType;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.auth.repository.UserAuthRepository;
import com.example.shimpyo.domain.user.repository.UserRepository;
import com.example.shimpyo.domain.utils.NicknamePrefixLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuth2Service {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;

    @Value("${spring.security.oauth2.client.provider.kakao.admin-key}")
    private String KAKAO_ADMIN_KEY;

    @Transactional
    public LoginResponseDto kakaoLogin(String accessToken) throws JsonProcessingException {
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
        // TODO 재가입시 로직
        UserAuth user = userAuthRepository.findByUserLoginIdAndSocialTypeAndDeletedAtIsNull(email, SocialType.KAKAO)
                .orElseGet(() -> {
                    User newUSer = userRepository.save(User.builder()
                            .email(email)
                            .nickname(NicknamePrefixLoader.generateNickNames())
                            .build());

                    return userAuthRepository.save(UserAuth.builder()
                            .user(newUSer)
                            .userLoginId(email)
                            .password(null)
                            .oauthId(id)
                            .socialType(SocialType.KAKAO)
                            .build());
                });

        return LoginResponseDto.toDto(user);
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
