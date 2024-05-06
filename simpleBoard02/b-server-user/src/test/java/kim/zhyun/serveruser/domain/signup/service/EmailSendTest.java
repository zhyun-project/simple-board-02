package kim.zhyun.serveruser.domain.signup.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import kim.zhyun.serveruser.container.RedisTestContainer;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.EmailAuthDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("이메일로 인증코드 전송")
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
public class EmailSendTest {
    
    @Autowired JavaMailSender mailSender;
    @Autowired RedisTemplate<String, String> redisTemplate;
    
    private String EMAIL_AUTH_CODE;
    
    @BeforeEach
    void beforeEach() {
        EMAIL_AUTH_CODE = UUID.randomUUID().toString().replace("-", "").substring(1, 7);
    }
    
    
    @DisplayName("실제 전송 후 redis에 인증코드 유효성 검사 테스트")
    @Test
    void sendEmailAuthCode_success() throws MessagingException, UnsupportedEncodingException, InterruptedException {
        // given
        String toAddress = "gimwlgus@gmail.com";
        MimeMessage mimeMessage = makeMailBody(mailSender, toAddress, EMAIL_AUTH_CODE);
        EmailAuthDto emailAuthDto = EmailAuthDto.builder()
                .email(toAddress)
                .code(EMAIL_AUTH_CODE)
                .build();
        
        // when
        // 메일 발송
        mailSender.send(mimeMessage);
        
        // redis에 email과 인증코드 저장 (유효시간 3초)
        int expireTime = 3;
        redisTemplate.opsForSet().add(emailAuthDto.getEmail(), emailAuthDto.getCode());
        redisTemplate.expire(emailAuthDto.getEmail(), expireTime, SECONDS);
        
        // then
        assertThat(redisTemplate.opsForSet().isMember(emailAuthDto.getEmail(), emailAuthDto.getCode()))
                .isTrue();
        
        Thread.sleep(expireTime * 1000); // 인증코드 만료 시간 기다림
        
        assertThat(redisTemplate.opsForSet().isMember(emailAuthDto.getEmail(), emailAuthDto.getCode()))
                .isFalse();
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
        <h1><center><span>%s</span></center></h3>
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

