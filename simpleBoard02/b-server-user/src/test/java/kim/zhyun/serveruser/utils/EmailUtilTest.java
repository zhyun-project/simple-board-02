package kim.zhyun.serveruser.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("EmailUtil 테스트")
@SpringBootTest
class EmailUtilTest {

    @Autowired EmailUtil emailUtil;
    
    @DisplayName("인증 코드 생성 형태 확인")
    @Test
    void i_want_see_the_auth_code() {
        // given-when
        String authCode = emailUtil.getAuthCode();
        
        // then
        assertAll(
                () -> assertNotNull(authCode),
                () -> assertTrue(Strings.isNotBlank(authCode))
        );
        
        log.info("auth code : {}", authCode);
    }
    
    @DisplayName("java mail sender 실행 테스트")
    @Test
    void send_email() {
        // given
        String toAddress    = "gimwlgus@daum.net";
        String fromAddress  = "gimwlgus@gmail.com";
        String fromName     = "SB2 프로젝트에서 보냄";
        String title        = "제목 : 테스트 발송";
        String body         = "<h1>테스트 발송 내용</h1>";

        Throwable throwable = null;
        
        // when
        try {
            MimeMessage message = emailUtil.createMessage(
                    toAddress, title, body, fromAddress, fromName
            );
            emailUtil.sendMail(message);
            
        } catch (MessagingException | UnsupportedEncodingException e) {
            throwable = new RuntimeException(e);
        }
        
        // then
        assertNull(throwable);
    }

}