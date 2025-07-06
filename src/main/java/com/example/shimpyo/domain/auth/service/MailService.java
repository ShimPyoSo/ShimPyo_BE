package com.example.shimpyo.domain.auth.service;

import com.example.shimpyo.domain.auth.dto.MailVerifyDto;
import com.example.shimpyo.global.BaseException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.example.shimpyo.global.exceptionType.AuthException.MAIL_CODE_NOT_MATCHED;

@Transactional
@RequiredArgsConstructor
@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // [#MOO4] 메일 전송 시작 
    public void authEmail(String email) {
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
}
