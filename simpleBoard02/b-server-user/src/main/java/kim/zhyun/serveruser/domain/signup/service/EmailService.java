package kim.zhyun.serveruser.domain.signup.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.serveruser.domain.member.service.SessionUserService;
import kim.zhyun.serveruser.domain.signup.business.model.SessionUserEmailUpdateDto;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.EmailAuthDto;
import kim.zhyun.serveruser.domain.signup.converter.EmailAuthConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_MAIL_SEND_FAIL;


@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {
    private final RedisTemplate<String, String> template;
    private final SessionUserService sessionUserService;
    private final JavaMailSender mailSender;
    
    private final EmailAuthConverter emailAuthConverter;
    
    
    @Value("${sign-up.email.expire}")   private long expireTime;
    
    /**
     * (redis) email auth
     * - email 존재 여부 반환 (이메일 인증을 마친 상태인지 확인 )
     */
    public boolean existEmail(EmailAuthDto dto) {
        return template.hasKey(dto.getEmail());
    }
    
    /**
     * 메일 전송
     */
    public void sendEmailAuthCode(String userEmail) {
        try {
            String authCode = getCode();
            
            MimeMessage message = createMessage(userEmail, authCode);
            mailSender.send(message);
            
            saveEmailAuthCode(emailAuthConverter.toDto(userEmail, authCode));
            
        } catch (Exception e) {
            throw new ApiException(EXCEPTION_MAIL_SEND_FAIL);
        }
    }
    
    /**
     * (redis) email auth
     * - email에 할당된 인증코드인지 여부 반환
     */
    public boolean existCode(EmailAuthDto dto) {
        return template.opsForSet().isMember(dto.getEmail(), dto.getCode());
    }
    
    
    /**
     * 1. (redis) email auth
     * - email 삭제
     * 2. (redis) session user
     * - email 할당
     */
    public void deleteAndUpdateSessionUserEmail(SessionUserEmailUpdateDto dto) {
        template.delete(dto.getEmail());
        sessionUserService.updateEmail(dto);
    }
    
    /**
     * (redis) email auth
     * - email과 인증 코드 신규 저장
     */
    private void saveEmailAuthCode(EmailAuthDto dto) {
        template.opsForSet().add(dto.getEmail(), dto.getCode());
        template.expire(dto.getEmail(), expireTime, SECONDS);
    }
    
    /**
     * mail 내용 작성
     */
    private MimeMessage createMessage(String to, String code) throws MessagingException, UnsupportedEncodingException {
        MimeMessage  message = mailSender.createMimeMessage();
        
        message.addRecipients(MimeMessage.RecipientType.TO, to);
        message.setSubject(String.format("[Simple Board 02] 회원가입 인증 코드 [%s]", code));
        
        String body = String.format("""
        <br>
        <hr>
        <br>
        <h1><center><span>💣 1분 안에 아래의 인증 코드를 입력해주세요 🤗</span></center></h1>
        <br>
        <h1><center><span>%s</span></center></h1>
        <br>
        <hr>
        <br>
        """, code);
        
        message.setText(body, "utf-8", "html");
        message.setFrom(new InternetAddress("no-reply@simpleboard.02","SB02-ADMIN"));
        return message;
    }
    
    /**
     * 인증 코드 생성
     */
    private String getCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(1, 7);
    }
    
}
