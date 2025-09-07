package com.example.shimpyo.domain.auth.service;

import com.example.shimpyo.domain.auth.jwt.JwtTokenProvider;
import com.example.shimpyo.domain.auth.dto.*;
import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.auth.repository.UserAuthRepository;
import com.example.shimpyo.domain.user.entity.SocialType;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.user.repository.UserRepository;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.domain.utils.CookieUtils;
import com.example.shimpyo.domain.utils.NicknamePrefixLoader;
import com.example.shimpyo.domain.utils.SecurityUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.shimpyo.global.exceptionType.AuthException.*;
import static com.example.shimpyo.global.exceptionType.MemberException.*;
import static com.example.shimpyo.global.exceptionType.MailException.*;
import static com.example.shimpyo.global.exceptionType.TokenException.INVALID_REFRESH_TOKEN;
import static com.example.shimpyo.global.exceptionType.TokenException.NOT_MATCHED_REFRESH_TOKEN;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthService {

    @Value("${jwt.expiration}")
    long expiration;
    @Value("${jwt.expirationALRT}")
    long expirationALRT; // 자동 로그인(RememberMe)일 때의 RT 유효시간(밀리초)
    @Value("${jwt.expirationRT}")
    long expirationRT;   // 일반 RT 유효시간(밀리초)

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final MailService mailService;
    private final OAuth2Service oAuth2Service;
    private final CookieUtils cookieUtils;

    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIALS = "~!@#$%^&*";
    private static final String ALL = LETTERS + NUMBERS + SPECIALS;
    private static final SecureRandom random = new SecureRandom();

    /* ================= 공통 유틸 ================ */

    private UserAuth getUserAuthOrThrow(String loginId) {
        return userAuthRepository.findByUserLoginId(loginId)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
    }

    private void issueTokensAndSetCookies(HttpServletResponse response, String username, UserAuth userAuth, boolean isRememberMe) {
        String accessToken = jwtTokenProvider.createAccessToken(username);
        String refreshToken = jwtTokenProvider.createRefreshToken(username, isRememberMe);

        // Redis: RT 저장 (키 = 로그인 아이디)
        redisService.saveRefreshToken(userAuth.getUserLoginId(), refreshToken);

        long refreshMaxAgeSec = (isRememberMe ? expirationALRT : expirationRT) / 1000L;

        cookieUtils.addCookies(response,
                cookieUtils.buildAccessCookie(accessToken, expiration / 1000L),
                cookieUtils.buildRefreshCookie(refreshToken, refreshMaxAgeSec)
        );

        userAuthRepository.updateLastLogin(username);
    }

    /* ================= 회원가입/이메일 ================= */

    // [#MOO1] 사용자 회원가입
    public LoginResponseDto registerUser(RegisterUserRequest dto, HttpServletResponse response) {
        // 삭제되지 않은 사용자 중 이메일 중복 체크
        if (userRepository.findByEmailAndDeletedAtIsNull(dto.getEmail()).isPresent()) {
            throw new BaseException(EMAIL_DUPLICATION);
        }

        User user = userRepository.save(dto.toUserEntity(NicknamePrefixLoader.generateNickNames()));
        UserAuth userAuth = userAuthRepository.save(dto.toUserAuthEntity(passwordEncoder.encode(dto.getPassword()), user));

        issueTokensAndSetCookies(response, dto.getUsername(), userAuth, false);

        return LoginResponseDto.toDto(user);
    }

    // [#MOO2] 이메일 인증(가용성 체크)
    public Map<String, Boolean> emailCheck(String email) {
        // 존재하면 중복 예외, 없으면 사용 가능
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BaseException(EMAIL_DUPLICATION);
        }
        return Map.of("EmailValidated", true);
    }

    /* ================= 재발급/로그아웃 ================= */
    // [#MOO6] AccessToken 재발급
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtils.getCookieValue(request);
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new BaseException(INVALID_REFRESH_TOKEN);
        }

        String userName = jwtTokenProvider.getUserNameFromRefresh(refreshToken);

        // Redis RT 일치 확인
        String savedToken = redisService.getRefreshToken(userName);
        if (!refreshToken.equals(savedToken)) {
            throw new BaseException(NOT_MATCHED_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userName);
        cookieUtils.addCookies(response,
                cookieUtils.buildAccessCookie(newAccessToken, expiration / 1000L));
    }

    public void logout(String accessToken, HttpServletResponse response) {
        // 1) AccessToken 블랙리스트
        long expiration = jwtTokenProvider.getAccessTokenRemainingExpiration(accessToken);
        redisService.tokenBlackList(accessToken, expiration);

        // 2) Redis의 RefreshToken 삭제 (키는 loginId 그대로)
        redisService.deleteRefreshToken(SecurityUtils.getLoginId());

        // 3) 쿠키 삭제
        cookieUtils.clearAuthCookies(response);
    }

    // 자동 로그인(리프레시만으로 사용자 정보 응답)
    public LoginResponseDto reLoginResponse(HttpServletRequest request) {
        String token = cookieUtils.getCookieValue(request);
        if (jwtTokenProvider.validateToken(token))
            throw new BaseException(INVALID_REFRESH_TOKEN);
        String username = jwtTokenProvider.getUserNameFromRefresh(token);

        User user = userAuthRepository.findByUserLoginId(username)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND)).getUser();
        return LoginResponseDto.toDto(user);
    }

    /* ================= 사용자 관리 ================= */

    public void validateDuplicateUsername(String username) {
        userAuthRepository.findByUserLoginId(username)
                .ifPresent(u -> { throw new BaseException(LOGIN_ID_DUPLICATION); });
    }

    public FindUsernameResponseDto findUsername(FindUsernameRequestDto dto) {
        String email = dto.getEmail();
        if (email == null || email.isBlank()) {
            throw new BaseException(INVALID_EMAIL_REQUEST);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(EMAIL_NOT_FOUNDED));

        UserAuth userAuth = userAuthRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        String username = userAuth.getUserLoginId();
        int length = username.length();
        int half = length / 2 + 1;
        String masked = username.substring(0, half) + "*".repeat(length - half);

        return FindUsernameResponseDto.builder()
                .username(masked)
                .createdAt(LocalDate.parse(userAuth.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                .build();
    }

    public void sendPasswordResetMail(FindPasswordRequestDto requestDto) throws MessagingException {
        UserAuth user = getUserAuthOrThrow(requestDto.getUsername());

        if (user.getOauthId() != null) throw new BaseException(SOCIAL_USER_CANT_CHANGE_PASSWORD);
        if (!user.getUser().getEmail().equals(requestDto.getEmail())) throw new BaseException(EMAIL_NOT_FOUNDED);

        String tempPW = generatePassword();
        mailService.sendResetPasswordMail(requestDto.getEmail(), tempPW);
        user.resetPassword(passwordEncoder.encode(tempPW));
    }

    public void resetPassword(ResetPasswordRequestDto requestDto) {
        UserAuth userAuth = getUserAuthOrThrow(SecurityUtils.getLoginId());

        if (userAuth.getOauthId() != null) throw new BaseException(SOCIAL_USER_CANT_CHANGE_PASSWORD);

        String nowPW = requestDto.getNowPassword();
        String newPW = requestDto.getNewPassword();
        String checkNewPW = requestDto.getCheckNewPassword();

        if (!passwordEncoder.matches(nowPW, userAuth.getPassword()))
            throw new BaseException(PASSWORD_NOT_MATCHED);
        if (!newPW.equals(checkNewPW))
            throw new BaseException(TWO_PASSWORD_NOT_MATCHED);
        if (passwordEncoder.matches(newPW, userAuth.getPassword()))
            throw new BaseException(PASSWORD_DUPLICATED);

        userAuth.resetPassword(passwordEncoder.encode(newPW));
    }

    public void deleteUser(HttpServletResponse response) {
        UserAuth userAuth = getUserAuthOrThrow(SecurityUtils.getLoginId());
        User user = userRepository.findByUserAuth(userAuth)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        if (userAuth.getDeletedAt() != null) throw new BaseException(MEMBER_NOT_FOUND);

        if (userAuth.getSocialType().equals(SocialType.KAKAO)) {
            oAuth2Service.unlinkKaKao(userAuth);
        }
        cookieUtils.clearAuthCookies(response);
        userAuthRepository.delete(userAuth);
        userRepository.delete(user);
    }

    public UserAuth findUser() {
        return getUserAuthOrThrow(SecurityUtils.getLoginId());
    }

    public Optional<UserAuth> findUserAuth() {
        return userAuthRepository.findByUserLoginId(SecurityUtils.getLoginId());
    }

    public void setMoreInfo(InfoRequestDto requestDto) {
        User user = findUser().getUser();
        user.changeGender(requestDto.getGender());
        user.changeBirthYear(requestDto.getBirthYear());
    }

    /* ================= 임시 비밀번호 생성 ================= */

    private static String generatePassword() {
        List<Character> passwordChars = new ArrayList<>();
        passwordChars.add(LETTERS.charAt(random.nextInt(LETTERS.length())));
        passwordChars.add(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        passwordChars.add(SPECIALS.charAt(random.nextInt(SPECIALS.length())));
        for (int i = 3; i < 8; i++) {
            passwordChars.add(ALL.charAt(random.nextInt(ALL.length())));
        }
        Collections.shuffle(passwordChars);
        StringBuilder password = new StringBuilder();
        for (char ch : passwordChars) password.append(ch);
        return password.toString();
    }
}
