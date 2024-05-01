package kim.zhyun.serveruser.domain.signup.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import kim.zhyun.serveruser.config.MailConfig;
import kim.zhyun.serveruser.container.RedisTestContainer;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.EmailAuthDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("email service test")
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    
    @InjectMocks EmailService emailService;
    @Mock RedisTemplate<String, String> redisTemplate;
    
    private final String email = "gimwlgus@gmail.com";
    
    
    @DisplayName("인증 코드가 할당된 이메일인지 확인 - 성공")
    @Test
    void existEmail_true() {
        // given
        String authCode = getCode();
        
        EmailAuthDto emailAuthDto = EmailAuthDto.builder()
                .email(email)
                .code(authCode)
                .build();
        
        // when
        when(redisTemplate.hasKey(emailAuthDto.getEmail())).thenReturn(true);
        
        // then
        assertThat(emailService.existEmail(emailAuthDto)).isTrue();
    }
    
    @DisplayName("인증 코드가 할당된 이메일인지 확인 - 실패")
    @Test
    void existEmail_false() {
        // given
        String authCode = getCode();
        EmailAuthDto emailAuthDto = EmailAuthDto.builder()
                .email("hello" + email)
                .code(authCode)
                .build();
        
        // when
        when(redisTemplate.hasKey(emailAuthDto.getEmail())).thenReturn(false);
        
        // then
        assertThat(emailService.existEmail(emailAuthDto)).isFalse();
    }
    
    
    @DisplayName("이메일로 인증코드 전송")
    @Import(MailConfig.class)
    @ExtendWith(RedisTestContainer.class)
    @SpringBootTest
    @Nested
    class SendEmail {
        @Autowired JavaMailSender mailSender;
        @Autowired RedisTemplate<String, String> redisTemplate;
        
        @DisplayName("실제 전송 후 redis에 인증코드 유효성 검사 테스트")
        @Test
        void sendEmailAuthCode_success() throws MessagingException, UnsupportedEncodingException, InterruptedException {
            // given
            String authCode = getCode();
            MimeMessage mimeMessage = makeMailBody(mailSender, email, authCode);
            EmailAuthDto emailAuthDto = EmailAuthDto.builder()
                    .email(email)
                    .code(authCode)
                    .build();
            
            // when
            // 메일 발송
            mailSender.send(mimeMessage);
            
            // redis에 email과 인증코드 저장 (유효시간 60초)
            redisTemplate.opsForSet().add(emailAuthDto.getEmail(), emailAuthDto.getCode());
            redisTemplate.expire(emailAuthDto.getEmail(), 60, SECONDS);
            
            // then
            assertThat(redisTemplate.opsForSet().isMember(emailAuthDto.getEmail(), emailAuthDto.getCode()))
                    .isTrue();
            
            Thread.sleep(60 * 1000); // 인증코드 만료 시간 기다림
            
            assertThat(redisTemplate.opsForSet().isMember(emailAuthDto.getEmail(), emailAuthDto.getCode()))
                    .isFalse();
        }
    }
    
    
    
    // 인증 코드 생성
    private String getCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(1, 7);
    }
    
    // mail body 생성
    private MimeMessage makeMailBody(JavaMailSender mailSender, String to, String code) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        
        message.addRecipients(MimeMessage.RecipientType.TO, to);
        message.setSubject(String.format("[Simple Board 02] 회원가입 테스트 인증 코드 [%s]", code));
        
        String body = String.format("""
        <br>
        <hr>
        <br>
        <h1><center><span>TESTtestTESTtestTESTtestTESTtestTESTtestTEST</span></center></h1>
        <h1><center><span>💣 1분 안에 아래의 인증 코드를 입력해주세요 🤗</span></center></h1>
        <br>
        <h3><center><span>%s</span></center></h3>
        <br>
        <h1><center><span>TESTtestTESTtestTESTtestTESTtestTESTtestTEST</span></center></h1>
        <hr>
        <br>
        """, code);
        
        message.setText(body, "utf-8", "html");
        message.setFrom(new InternetAddress("no-reply@simpleboard.02","SB02-ADMIN"));
        return message;
    }
}

