package com.example.shimpyo.domain.auth.service;

import com.example.shimpyo.domain.auth.dto.MailVerifyDto;
import com.example.shimpyo.domain.auth.dto.MailCodeSendDto;
import com.example.shimpyo.domain.auth.entity.UserAuth;
import com.example.shimpyo.domain.user.repository.UserRepository;
import com.example.shimpyo.global.BaseException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.example.shimpyo.global.exceptionType.AuthException.MAIL_CODE_NOT_MATCHED;
import static com.example.shimpyo.global.exceptionType.MemberExceptionType.EMAIL_DUPLICATION;
import static com.example.shimpyo.global.exceptionType.MemberExceptionType.EMAIL_NOT_FOUNDED;

@Transactional
@RequiredArgsConstructor
@Service
public class MailService {

    private final JavaMailSender mailSender;
    @Qualifier("1")
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    
    // [#MOO4] 메일 전송 시작 
    public void authEmail(MailCodeSendDto dto) {
        String email = "";
        
        /*
        * 메일 전송 타입 : register 인 경우 메일이 존재할 경우 : "이미 가입된 이메일 입니다." 예외 처리
        * 메일 전송 타입 : find 인 경우 메일이 존재하지 않을 경우 : "해당 이메일이 존재하지 않습니다." 예외 처리
         */
        if(dto.getType().equals("register")){
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new BaseException(EMAIL_DUPLICATION);
            }else{
                email = dto.getEmail();
            }
        }else if(dto.getType().equals("find")){
            email = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new BaseException(EMAIL_NOT_FOUNDED)).getEmail();
        }

        Random random = new Random();

        StringBuilder authkey = new StringBuilder();
        for(int i = 0 ; i < 3 ; i++){
            char letter = (char) (random.nextInt(26) + 'A');
            authkey.append(letter);
        }
        authkey.append(String.valueOf(random.nextInt(888) + 111));

        try {
            sendAuthEmail(email, authkey.toString());
            // 5분 5초의 제한 시간으로 레디스에 인증 코드 저장
            redisTemplate.opsForValue().set(email, authkey.toString(), 305, TimeUnit.SECONDS);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendAuthEmail(String email, String authKey) throws MessagingException {
        String subject = "ShimPyoSo Authorization";
        String text = "인증번호는 " + authKey + "입니다.";

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(text, true); // 포함된 텍스트가 HTML이라는 의미로 true.
        mailSender.send(mimeMessage);
    }
    // [#MOO4] 메일 전송 끝

    // [#MOO5] 메일 정보와 인증 코드 일치 여부 판단 로직 시작
    public void verifyAuthCode(MailVerifyDto dto) {
        String storedAuthKey =  (String) redisTemplate.opsForValue().get(dto.getEmail());
        if(!dto.getAuthKey().equals(storedAuthKey))
            throw new BaseException(MAIL_CODE_NOT_MATCHED);
        else{
            redisTemplate.delete(dto.getEmail());
        }
    }
    // [#MOO5] 메일 정보와 인증 코드 일치 여부 판단 로직 끝

    // 회원 이메일로 임시 비밀번호 전송
    public void sendResetPasswordMail(String email, String tempPW) throws MessagingException {

        String subject = "ShimPyoSo Authorization";
        String text = "임시 비밀번호는 " + tempPW + "입니다.";

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(text, true); // 포함된 텍스트가 HTML이라는 의미로 true.
        mailSender.send(mimeMessage);
    }


}
