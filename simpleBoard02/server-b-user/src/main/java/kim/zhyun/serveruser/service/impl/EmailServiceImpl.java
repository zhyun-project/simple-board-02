package kim.zhyun.serveruser.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import kim.zhyun.serveruser.advice.MailAuthException;
import kim.zhyun.serveruser.data.EmailAuthDto;
import kim.zhyun.serveruser.data.SessionUserEmailUpdate;
import kim.zhyun.serveruser.service.EmailService;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static kim.zhyun.serveruser.data.type.ExceptionType.MAIL_SEND_FAIL;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailServiceImpl implements EmailService {
    private final RedisTemplate<String, String> template;
    private final SessionUserService sessionUserService;
    private final JavaMailSender mailSender;
    
    
    @Value("${sign-up.email.expire}")   private long expireTime;
    
    @Override
    public boolean existEmail(EmailAuthDto dto) {
        return template.hasKey(dto.getEmail());
    }
    
    @Override
    public void sendEmailAuthCode(String userEmail) {
        try {
            String authCode = getCode();
            EmailAuthDto saveEmailInfo = EmailAuthDto.builder()
                    .email(userEmail)
                    .code(authCode).build();
            
            MimeMessage message = createMessage(userEmail, authCode);
            mailSender.send(message);
            
            saveEmailAuthCode(saveEmailInfo);
            
        } catch (Exception e) {
            throw new MailAuthException(MAIL_SEND_FAIL);
        }
    }
    
    @Override
    public boolean existCode(EmailAuthDto dto) {
        return template.opsForSet().isMember(dto.getEmail(), dto.getCode());
    }
    
    @Override
    public void saveEmailAuthCode(EmailAuthDto dto) {
        template.opsForSet().add(dto.getEmail(), dto.getCode());
        template.expire(dto.getEmail(), expireTime, SECONDS);
    }
    
    @Override
    public void deleteAndUpdateSessionUserEmail(EmailAuthDto dto, String sessionId) {
        SessionUserEmailUpdate sessionUserEmailUpdate = SessionUserEmailUpdate.builder()
                .id(sessionId)
                .email(dto.getEmail())
                .emailVerification(true).build();
        
        template.delete(dto.getEmail());
        sessionUserService.updateEmail(sessionUserEmailUpdate);
    }
    
    private MimeMessage createMessage(String to, String code) throws MessagingException, UnsupportedEncodingException {
        MimeMessage  message = mailSender.createMimeMessage();
        
        message.addRecipients(MimeMessage.RecipientType.TO, to);
        message.setSubject(String.format("[Simple Board 02] 회원가입 인증 코드 [%s]", code));
        
        String body = String.format("""
        <br>
        <hr>
        <br>
        <h1><center><span>💣 1분 안에 아래의 인증 코드를 입력해주세요 🤗</span></center></h1>
        <h3><center><span>%s</span></center></h3>
        <br>
        <hr>
        <br>
        """, code);
        
        message.setText(body, "utf-8", "html");
        message.setFrom(new InternetAddress("no-reply@simpleboard.02","SB02-ADMIN"));
        return message;
    }
    
    private String getCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(1, 7);
    }
}
