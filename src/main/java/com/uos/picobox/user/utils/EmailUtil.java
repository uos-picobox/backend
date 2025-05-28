package com.uos.picobox.user.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class EmailUtil {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String sender;

    public MimeMessage createMail(String email, String title, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(email);
        helper.setSubject("[Pico Box] " + title);
        helper.setText(body, true);
        // 실제로 존재하는 주소. 인증 이메일 주소와 달라도 됨. SMTP 서버 기본 주소 존재 시 없어도 됨
        helper.setFrom(sender);
        return message;
    }

    public void sendMail(MimeMessage message) {
        mailSender.send(message);
    }

    public String createAuthCode() {
        int CODE_LENGTH = 6;
        Random random = new Random();

        char[] code = new char[CODE_LENGTH];
        for (int i = 0; i< CODE_LENGTH; i++) {
            int edge = random.nextInt(2);
            if (edge == 0) {
                code[i] = (char) ('0' + random.nextInt(10));
                continue;
            }
            code[i] = (char) ('A' + random.nextInt(26));
        }
        return new String(code);
    }
}
