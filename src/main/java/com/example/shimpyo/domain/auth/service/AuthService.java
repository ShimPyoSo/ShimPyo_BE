package com.example.shimpyo.domain.auth.service;

import com.example.shimpyo.domain.auth.JwtTokenProvider;
import com.example.shimpyo.domain.auth.dto.*;
import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.auth.repository.UserAuthRepository;
import com.example.shimpyo.domain.user.entity.SocialType;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.user.repository.UserRepository;
import com.example.shimpyo.domain.user.utils.RedisService;
import com.example.shimpyo.domain.utils.NicknamePrefixLoader;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.utils.SecurityUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.shimpyo.global.exceptionType.AuthException.*;
import static com.example.shimpyo.global.exceptionType.MemberExceptionType.*;
import static com.example.shimpyo.global.exceptionType.TokenException.INVALID_REFRESH_TOKEN;
import static com.example.shimpyo.global.exceptionType.TokenException.NOT_MATCHED_REFRESH_TOKEN;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthService {

    @Value("${jwt.expirationALRT}")
    long expirationALRT;
    // 자동 로그인 체크 X 시 유효시간 2시간.
    @Value("${jwt.expirationRT}")
    long expirationRT;

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final MailService mailService;
    private final OAuth2Service oAuth2Service;

    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIALS = "~!@#$%^&*";
    private static final String ALL = LETTERS + NUMBERS + SPECIALS;
    private static final SecureRandom random = new SecureRandom();

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
    public LoginResponseDto login(UserLoginDto dto, HttpServletResponse response) {

        // 1. 사용자 검증
        UserAuth userAuth = userAuthRepository.findByUserLoginId(dto.getUsername())
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        // [#MOO5] 토큰 발급 로직 수정 시작
        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(dto.getPassword(), userAuth.getPassword())) {
            throw new BaseException(MEMBER_INFO_NOT_MATCHED);
        }

        // 3. 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(dto.getUsername(), userAuth.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(dto.getUsername(), userAuth.getId(), dto.getIsRememberMe());

        // 4. RefreshToken Redis 저장.
        redisService.saveRefreshToken(userAuth.getUserLoginId(), refreshToken);

        // 리프레시 토큰 유효시간
        long refreshTokenExpire = dto.getIsRememberMe() ? expirationALRT / 1000L : expirationRT / 1000L;
        // 쿠키 설정
        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                // secure(true) 라면 https 에서만 쿠키 전송
                .secure(false)
                .path("/")
                .maxAge(1800)// 30분
                // 타사이트 요청시 쿠키 전송 X
                // Lax : 안정한 타사이트 요청 (GET)에만 허용
                .sameSite("Lex")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                // secure(true) 라면 https 에서만 쿠키 전송
                .secure(false)
                .path("/")
                .maxAge(refreshTokenExpire) // 30일
                .sameSite("Lex")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        // [#MOO5] 토큰 발급 로직 수정 끝

        userAuthRepository.updateLastLogin(dto.getUsername());
        return LoginResponseDto.toDto(userAuth);
    }
    // [#MOO3] 유저 로그인 끝

    // [#MOO6] access Token 재발급 로직
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 refresh_token 추출
        String refreshToken = extractCookie(request);
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new BaseException(INVALID_REFRESH_TOKEN);
        }

        String userName = jwtTokenProvider.getUserNameToRefresh(refreshToken);
        long userId = jwtTokenProvider.getUserIdToRefresh(refreshToken);

        // 3. redis에 저장된 토큰과 비교
        String savedToken = redisService.getRefreshToken(userName);
        if(!refreshToken.equals(savedToken)) {
            throw new BaseException(NOT_MATCHED_REFRESH_TOKEN);
        }

        // 4. 새 AccessToken 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(userName, userId);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", newAccessToken)
                .httpOnly(true)
                // secure(true) 라면 https 에서만 쿠키 전송
                .secure(false)
                .path("/")
                .maxAge(1800)
                .sameSite("Lex")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
    }

    private String extractCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for(Cookie cookie : request.getCookies()){
            if(cookie.getName().equals("refresh_token")){
                return cookie.getValue();
            }
        }
        return null;
    }
    // [#MOO6] access Token 재발급 로직

    // 로그인 아이디가 중복된 아이디인지 아닌지 판별하는 로직
    public void validateDuplicateUsername(String username){
        userAuthRepository.findByUserLoginId(username)
            .ifPresent(user -> {
                throw new BaseException(LOGIN_ID_DUPLICATION);
            });
    }

    // 유저 아이디 찾는 로직
    public FindUsernameResponseDto findUsername(FindUsernameRequestDto dto) {
        String email = dto.getEmail();

        if(email == null || email.isBlank()){
            throw new BaseException(INVALID_EMAIL_REQUEST);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(EMAIL_NOT_FOUNDED));

        UserAuth userAuth = userAuthRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        String username = userAuth.getUserLoginId();
        int length = username.length();
        int half = length/2+1;

        String masked = username.substring(0,half) + "*".repeat(length-half);


        return FindUsernameResponseDto.builder()
                .username(masked)
                .createdAt(LocalDate.parse(userAuth.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                .build();
    }

    public void sendPasswordResetMail(FindPasswordRequestDto requestDto) throws MessagingException {

        UserAuth user = userAuthRepository.findByUserLoginId(requestDto.getUsername())
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        if (!user.getUser().getEmail().equals(requestDto.getEmail()))
            throw new BaseException(EMAIL_NOT_FOUNDED);

        String tempPW = generatePassword();
        mailService.sendResetPasswordMail(requestDto.getEmail(),tempPW);
        user.resetPassword(passwordEncoder.encode(tempPW));
    }

    public void resetPassword(ResetPasswordRequestDto requestDto) {
        UserAuth userAuth = userAuthRepository.findByUserLoginId(SecurityUtils.getLoginId())
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        String nowPW = requestDto.getNowPassword();
        String newPW = requestDto.getNewPassword();
        String checkNewPW = requestDto.getCheckNewPassword();

        // 2. 현재 비밀번호 일치 확인
        if (!passwordEncoder.matches(nowPW, userAuth.getPassword())) {
            throw new BaseException(PASSWORD_NOT_MATCHED);
        }
        if (!newPW.equals(checkNewPW))
            throw new BaseException(TWO_PASSWORD_NOT_MATCHED);

        if (passwordEncoder.matches(newPW, userAuth.getPassword()))
            throw new BaseException(PASSWORD_DUPLICATED);
        userAuth.resetPassword(passwordEncoder.encode(newPW));
    }

    public void deleteUser() {
        UserAuth userAuth = userAuthRepository.findByUserLoginId(SecurityUtils.getLoginId())
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        User user = userRepository.findByUserAuth(userAuth)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        if (userAuth.getDeletedAt() != null)
            throw new BaseException(MEMBER_NOT_FOUND);

        if (userAuth.getSocialType().equals(SocialType.KAKAO))
            oAuth2Service.unlinkKaKao(userAuth);

        userAuthRepository.delete(userAuth);
        userRepository.delete(user);
    }

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
        for (char ch : passwordChars) {
            password.append(ch);
        }
        return password.toString();
    }

    // 로그아웃 메서드
    public void logout(String accessToken, HttpServletResponse response){
        // 1. AccessToken 블랙리스트 등록
        long expiration = jwtTokenProvider.getRemainingExpiration(accessToken);
        redisService.tokenBlackList(accessToken, expiration);

        // 2. redis 의 refresh_token 삭제
        redisService.deleteRefreshToken("refresh_token" + SecurityUtils.getLoginId());

        // 3. AccessToken/RefreshToken 을 쿠키에서 삭제
        ResponseCookie expiredAccessToken = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lex")
                .build();

        ResponseCookie expiredRefreshToken = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lex")
                .build();

        response.addHeader("Set-Cookie", expiredAccessToken.toString());
        response.addHeader("Set-Cookie", expiredRefreshToken.toString());
    }

    // 자동 로그인 반환 값
    public LoginResponseDto reLoginResponse(HttpServletRequest request) {
        String token = extractCookie(request);
        long userId = jwtTokenProvider.getUserIdToRefresh(token);

        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        return LoginResponseDto.toDto(user);
    }


    public UserAuth findUser() {
        return userAuthRepository.findByUserLoginId(SecurityUtils.getLoginId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
    }
}
