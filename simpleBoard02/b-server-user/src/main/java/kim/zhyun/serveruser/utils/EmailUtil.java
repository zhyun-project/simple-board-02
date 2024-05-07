package kim.zhyun.serveruser.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class EmailUtil {
    public final String EMAIL_AUTH_TITLE_FORM_NEED_AUTH_CODE = "[Simple Board 02] 회원가입 인증 코드 [%s]";
    public final String EMAIL_AUTH_BODY_FORM_NEED_AUTH_CODE = """
                    <br>
                    <hr>
                    <br>
                    <h1><center><span>💣 1분 안에 아래의 인증 코드를 입력해주세요 🤗</span></center></h1>
                    <br>
                    <h1><center><span>%s</span></center></h1>
                    <br>
                    <hr>
                    <br>
                    """;
    
    private final JavaMailSender mailSender;
    
    
    /**
     * mail 내용 작성
     */
    public MimeMessage createMessage(String to, String title, String body, String from, String fromName) throws MessagingException, UnsupportedEncodingException {
        MimeMessage  message = mailSender.createMimeMessage();
        
        message.addRecipients(MimeMessage.RecipientType.TO, to);
        message.setSubject(title);
        message.setText(body, "utf-8", "html");
        message.setFrom(new InternetAddress(from, fromName));
        
        return message;
    }
    
    /**
     * mail 발송
     */
    public void sendMail(MimeMessage message) {
        mailSender.send(message);
    }
    
    
    /**
     * 인증 코드 생성
     */
    public String getAuthCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(1, 7);
    }
}
