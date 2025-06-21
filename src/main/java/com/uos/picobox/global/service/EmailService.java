package com.uos.picobox.global.service;

import com.uos.picobox.user.dto.AuthMailRequestDto;
import com.uos.picobox.user.dto.MailRequestDto;
import com.uos.picobox.global.utils.EmailUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailUtils emailUtils;
    private final CacheManager cacheManager;

    public void sendAuthMail(MailRequestDto mailRequestDto) throws MessagingException {
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
        String formattedExpiration = expirationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String email = mailRequestDto.getEmail();
        String purpose = mailRequestDto.getPurpose();
        String authCode = emailUtils.createAuthCode();
        String body = """
            <html>
              <body style="font-family: 'Apple SD Gothic Neo', Arial, sans-serif; background-color: #f6f6f6; padding: 40px;">
                <div style="max-width: 600px; margin: auto; background-color: #fff; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); padding: 40px; text-align: center;">
                  <div style="display: flex; justify-content: center; gap: 2px; margin-bottom: 30px;">
                    <span style="background-color: #2D6FF2; color: white; font-size: 30px; font-weight: bold; width: 40px; height: 52px; display: flex; align-items: center; justify-content: center; border-radius: 6px; letter-spacing: 1px; font-family: 'Arial Black', 'Arial Bold', Gadget, sans-serif;">P</span>
                    <span style="background-color: #2D6FF2; color: white; font-size: 30px; font-weight: bold; width: 40px; height: 52px; display: flex; align-items: center; justify-content: center; border-radius: 6px; letter-spacing: 1px; font-family: 'Arial Black', 'Arial Bold', Gadget, sans-serif;">I</span>
                    <span style="background-color: #2D6FF2; color: white; font-size: 30px; font-weight: bold; width: 40px; height: 52px; display: flex; align-items: center; justify-content: center; border-radius: 6px; letter-spacing: 1px; font-family: 'Arial Black', 'Arial Bold', Gadget, sans-serif;">C</span>
                    <span style="background-color: #2D6FF2; color: white; font-size: 30px; font-weight: bold; width: 40px; height: 52px; display: flex; align-items: center; justify-content: center; border-radius: 6px; letter-spacing: 1px; font-family: 'Arial Black', 'Arial Bold', Gadget, sans-serif;">O</span>
                    <span style="background-color: #2D6FF2; color: white; font-size: 30px; font-weight: bold; width: 40px; height: 52px; display: flex; align-items: center; justify-content: center; border-radius: 6px; letter-spacing: 1px; font-family: 'Arial Black', 'Arial Bold', Gadget, sans-serif;">B</span>
                    <span style="background-color: #2D6FF2; color: white; font-size: 30px; font-weight: bold; width: 40px; height: 52px; display: flex; align-items: center; justify-content: center; border-radius: 6px; letter-spacing: 1px; font-family: 'Arial Black', 'Arial Bold', Gadget, sans-serif;">O</span>
                    <span style="background-color: #2D6FF2; color: white; font-size: 30px; font-weight: bold; width: 40px; height: 52px; display: flex; align-items: center; justify-content: center; border-radius: 6px; letter-spacing: 1px; font-family: 'Arial Black', 'Arial Bold', Gadget, sans-serif;">X</span>
                  </div>
                  <p style="color: #333; font-size: 16px; margin-bottom: 10px;"><strong>Pico Box %s</strong>을 위해 인증번호를 보내드려요.<br/>이메일 인증 화면에서 아래의 인증 번호를 입력하고 인증을 완료해주세요.</p>
                  <p style="font-size: 32px; font-weight: bold; color: #2D6FF2; letter-spacing: 4px; margin: 30px auto;">%s</p>
                  <p style="color: #999; font-size: 14px; margin-bottom: 4px;">이 인증번호는 5분 후 만료됩니다.</p>
                  <p style="color: #999; font-size: 14px; margin-bottom: 30px;">만료 시간: %s</p>
                  <p style="color: #555; font-size: 14px; line-height: 1.6;">
                    혹시 요청하지 않은 인증 메일을 받으셨나요?<br/>
                    누군가 실수로 메일 주소를 잘못 입력했을 수 있어요. 계정이 도용된 것은 아니니 안심하세요.<br/>
                    직접 요청한 인증 메일이 아닌 경우 무시해주세요.
                  </p>
                  <hr style="margin: 40px 0; border: none; border-top: 1px solid #eee;" />
                  <p style="color: #666; font-size: 13px;">이 메일은 발신 전용 메일이에요.</p>
                  <p style="color: #777; font-size: 11px;">Copyright © Pico Box All rights reserved.</p>
                </div>
              </body>
            </html>
            """.formatted(purpose, authCode, formattedExpiration);
        MimeMessage newMail = emailUtils.createMail(email,purpose +" 인증 번호를 보내드려요.", body);
        emailUtils.sendMail(newMail);
        Cache cache = cacheManager.getCache("emailAuthCode");
        Objects.requireNonNull(cache).put(email, authCode);
    }

    public void checkAuthCode(AuthMailRequestDto authMailRequestDto) {
        String email = authMailRequestDto.getEmail();
        String code = authMailRequestDto.getCode();
        Cache cache = cacheManager.getCache("emailAuthCode");
        Cache.ValueWrapper wrapper = Objects.requireNonNull(cache).get(email);
        if (Objects.isNull(wrapper)) {
            throw new IllegalArgumentException("인증 코드가 만료되었거나 존재하지 않습니다.");
        }
        String storedCode = (String) wrapper.get();
        if (!Objects.equals(code, storedCode)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }
        cache.evict(email);
    }
}
