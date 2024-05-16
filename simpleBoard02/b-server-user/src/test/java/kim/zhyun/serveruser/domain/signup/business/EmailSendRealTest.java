package kim.zhyun.serveruser.domain.signup.business;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kim.zhyun.serveruser.container.RedisTestContainer;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.EmailAuthDto;
import kim.zhyun.serveruser.domain.signup.service.EmailService;
import kim.zhyun.serveruser.utils.EmailUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;


@Disabled("필요시 실행")
@Order(0)
@DisplayName("이메일로 인증코드 실제 전송 테스트")
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
public class EmailSendRealTest {
    
    @Autowired EmailUtil emailUtil;
    @Autowired EmailService emailService;
    
    @Value("${sign-up.email.expire}") long expireTime;

    
    @DisplayName("실제 전송 후 redis에 인증코드 유효성 검사 테스트")
    @Test
    void sendEmailAuthCode_success() throws MessagingException, UnsupportedEncodingException, InterruptedException {
        
        // given
        String authCode = emailUtil.getAuthCode();
        String toAddress = "gimwlgus@gmail.com";
        String title = emailUtil.EMAIL_AUTH_TITLE_FORM_NEED_AUTH_CODE.formatted(authCode);
        String body = emailUtil.EMAIL_AUTH_BODY_FORM_NEED_AUTH_CODE.formatted(authCode);
        String fromAddress = "ADMIN@email.address";
        String fromName = "SB2 ADMIN";
        
        MimeMessage mimeMessage = emailUtil.createMessage(toAddress, title, body, fromAddress, fromName);
        
        EmailAuthDto emailAuthDto = EmailAuthDto.builder()
                .email(toAddress)
                .code(authCode)
                .build();
        
        // when
        // 메일 발송
        emailUtil.sendMail(mimeMessage);
        
        // redis에 email과 인증코드 저장 (유효시간 3초)
        emailService.saveEmailAuthInfo(emailAuthDto);
        
        // then
        assertThat(emailService.existCode(emailAuthDto)).isTrue();
        
        Thread.sleep(expireTime * 1000); // 인증코드 만료 시간 기다림
        
        assertThat(emailService.existCode(emailAuthDto)).isFalse();
    }
    
}

