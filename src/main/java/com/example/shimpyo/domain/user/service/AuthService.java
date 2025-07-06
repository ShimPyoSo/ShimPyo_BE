package com.example.shimpyo.domain.user.service;

import com.example.shimpyo.domain.auth.dto.UserLoginDto;
import com.example.shimpyo.domain.user.dto.LoginResponseDto;
import com.example.shimpyo.domain.user.dto.RegisterUserRequest;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.user.entity.UserAuth;
import com.example.shimpyo.domain.user.oauth.NicknamePrefixLoader;
import com.example.shimpyo.domain.user.repository.UserAuthRepository;
import com.example.shimpyo.domain.user.repository.UserRepository;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.MemberExceptionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.example.shimpyo.global.exceptionType.MemberExceptionType.EMAIL_DUPLICATION;
import static com.example.shimpyo.global.exceptionType.MemberExceptionType.MEMBER_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;

    // [#MOO1] 사용자 회원가입 시작
    public void registerUser(RegisterUserRequest dto) {
        // [#MOO1] 이메일 중복 여부 확인 (deleted_at = null 인 사용자만 대상으로)
        if (userRepository.findByEmailAndDeletedAtIsNull(dto.getEmail()).isPresent()) {
            throw new BaseException(EMAIL_DUPLICATION);
        }
        // 삭제한 사용자는 우쨤?

        // [#MOO1] 회원 등록 (비밀번호는 인코딩해서 저장)
        User user = userRepository.save(dto.toUserEntity(NicknamePrefixLoader.generateNickNames()));
        userAuthRepository.save(dto.toUserAuthEntity(passwordEncoder.encode(dto.getPassword()), user));
    }
    // [#MOO1] 사용자 회원가입 끝

    // [#MOO2] 이메일 인증 시작
    public Map<String, Boolean> emailCheck(String email) {
        userRepository.findByEmail(email).orElseThrow(() -> new BaseException(EMAIL_DUPLICATION));
        Map<String, Boolean> response = new HashMap<>();
        response.put("EmailValidated", true);

        return response;
    }
    // [#MOO2] 이메일 인증 끝

    // [#MOO3] 유저 로그인 시작
    public LoginResponseDto login(UserLoginDto dto){
        UserAuth userAuth = userAuthRepository.findByUserLoginId(dto.getUsername())
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        userAuthRepository.updateLastLogin(dto.getUsername());
        return LoginResponseDto.toDto(userAuth);
    }
    // [#MOO3] 유저 로그인 끝
}
