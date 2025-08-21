package com.example.shimpyo.domain.auth.service;

import com.example.shimpyo.domain.auth.dto.MailCodeSendDto;
import com.example.shimpyo.domain.auth.dto.MailVerifyDto;
import com.example.shimpyo.domain.user.entity.User;
import com.example.shimpyo.domain.user.repository.UserRepository;
import com.example.shimpyo.global.BaseException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import static com.example.shimpyo.global.exceptionType.MailException.*;

@Transactional
@Service
public class MailService {

    private final JavaMailSender mailSender;
    @Qualifier("1")
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    public MailService(JavaMailSender mailSender, RedisTemplate<String, Object> redisTemplate, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }


    // [#MOO4] 메일 전송 시작 
    public void authEmail(MailCodeSendDto dto) {

        /*
        * 메일 전송 타입 : register 인 경우 메일이 존재할 경우 : "이미 가입된 이메일 입니다." 예외 처리
        * 메일 전송 타입 : find 인 경우 메일이 존재하지 않을 경우 : "해당 이메일이 존재하지 않습니다." 예외 처리
         */
        Optional<User> findUser = userRepository.findByEmail(dto.getEmail());
        if(dto.getType().equals("register")){
            if (findUser.isPresent()) {
                throw new BaseException(EMAIL_DUPLICATION);
            }
        } else if(dto.getType().equals("find")){
            if (findUser.isEmpty())
                throw new BaseException(EMAIL_NOT_FOUNDED);
        }
        String email = dto.getEmail();

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
        String subject = "[쉼표] 이메일 주소 확인을 위한 인증 메일입니다";
        String content = "안녕하세요, <b>[쉼표]</b>에 가입해주셔서 감사합니다.<br>" +
                "아래 인증 번호를 입력해주세요.<br>" +
                "<div class='highlight'>" + authKey + "</div><br>" +
                "보안을 위해 이 메일은 타인과 공유하지 마시기 바랍니다.";
        String text = buildMailTemplate("회원가입 이메일 인증", content);

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

        redisTemplate.delete(dto.getEmail());
        redisTemplate.opsForValue().set("verified:email" + dto.getEmail(), "true", Duration.ofMinutes(10));

    }
    // [#MOO5] 메일 정보와 인증 코드 일치 여부 판단 로직 끝

    // 회원 이메일로 임시 비밀번호 전송
    public void sendResetPasswordMail(String email, String tempPW) throws MessagingException {
        Object verified = redisTemplate.opsForValue().get("verified:email" + email);

        if(verified == null || !"true".equals(verified.toString())){
            throw new BaseException(EMAIL_NOT_VERIFIED);
        }

        String subject = "[쉼표] 비밀번호 재설정을 위한 안내 메일입니다";
        String content = "안녕하세요, <b>[쉼표]</b>를 이용해주셔서 감사합니다.<br>" +
                "요청하신 계정의 임시 비밀번호는 아래와 같습니다." +
                "<div class='highlight'>" + tempPW + "</div><br>" +
                "로그인 후 반드시 비밀번호를 변경해 주세요.<br>" +
                "보안을 위해 이 메일은 타인과 공유하지 마시기 바랍니다.";

        String text = buildMailTemplate("비밀번호 재설정 안내", content);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(text, true); // 포함된 텍스트가 HTML이라는 의미로 true.
        mailSender.send(mimeMessage);
    }


    public String buildMailTemplate(String title, String content) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "  body { font-family: Arial, sans-serif; background-color:#f9f9f9; margin:0; padding:0; }" +
                "  .container { max-width:600px; margin:20px auto; background:#ffffff; border-radius:12px; box-shadow:0 4px 12px rgba(0,0,0,0.1); padding:30px; border:2px solid #80A381; }" +
                "  .title { font-size:22px; font-weight:bold; color:#80A381; margin-bottom:20px; text-align:center; }" +
                "  .content { font-size:15px; color:#333333; line-height:1.8; }" +
                "  .highlight { font-size:18px; font-weight:bold; color:#ffffff; background:#80A381; padding:12px 18px; border-radius:8px; display:inline-block; margin:20px 0; }" +
                "  .button { display:inline-block; background:#80A381; color:#ffffff !important; padding:12px 20px; margin:20px 0; border-radius:8px; text-decoration:none; font-weight:bold; }" +
                "  .footer { font-size:12px; color:#888888; margin-top:30px; text-align:center; border-top:1px solid #eeeeee; padding-top:15px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "  <div class='container'>" +
                "    <div class='title'>" + title + "</div>" +
                "    <div class='content'>" +
                content +
                "    </div>" +
                "    <div class='footer'>" +
                "      본 메일은 발신 전용 메일입니다.<br>" +
                "      ⓒ 2025 쉼표. All rights reserved." +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

}
