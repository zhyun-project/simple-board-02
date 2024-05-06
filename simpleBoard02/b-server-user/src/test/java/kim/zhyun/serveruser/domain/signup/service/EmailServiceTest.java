package kim.zhyun.serveruser.domain.signup.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import kim.zhyun.serveruser.domain.member.service.SessionUserService;
import kim.zhyun.serveruser.domain.signup.business.model.SessionUserEmailUpdateDto;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.EmailAuthDto;
import kim.zhyun.serveruser.domain.signup.converter.EmailAuthConverter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@Slf4j
@DisplayName("email service test")
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    
    @InjectMocks EmailService emailService;
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock JavaMailSender javaMailSender;
    @Mock EmailAuthConverter emailAuthConverter;
    @Mock SessionUserService sessionUserService;
    
    
    private final String TO_ADDRESS = "gimwlgus@gmail.com";
    private String EMAIL_AUTH_CODE;
    
    
    
    @DisplayName("인증 코드 생성 및 형태 확인용")
    @BeforeEach
    void show_me_the_email_auth_code() {
        EMAIL_AUTH_CODE = UUID.randomUUID().toString().replace("-", "").substring(1, 7);
        
        log.info("인증 코드 : {}", EMAIL_AUTH_CODE);
    }
    
    
    
    @DisplayName("인증 코드가 할당된 이메일인지 확인 - 성공")
    @Test
    void existEmail_true() {
        // given
        EmailAuthDto emailAuthDto = EmailAuthDto.builder()
                .email(TO_ADDRESS)
                .code(EMAIL_AUTH_CODE)
                .build();
        
        given(redisTemplate.hasKey(emailAuthDto.getEmail())).willReturn(true);
        
        // when
        boolean result = emailService.existEmail(emailAuthDto);
        
        // then
        assertTrue(result);
        then(redisTemplate).should(times(1)).hasKey(any());
    }
    
    @DisplayName("인증 코드가 할당된 이메일인지 확인 - 실패")
    @Test
    void existEmail_false() {
        // given
        EmailAuthDto emailAuthDto = EmailAuthDto.builder()
                .email("hello" + TO_ADDRESS)
                .code(EMAIL_AUTH_CODE)
                .build();
        given(redisTemplate.hasKey(emailAuthDto.getEmail())).willReturn(false);
        
        // when
        boolean result = emailService.existEmail(emailAuthDto);
        
        // then
        assertFalse(result);
        then(redisTemplate).should(times(1)).hasKey(any());
    }
    
    @DisplayName("이메일 전송 과정 검증")
    @Test
    void send_email() throws MessagingException, UnsupportedEncodingException {
        // given
        // -- 메일 내용 작성
        MimeMessage mimeMessage = mock(MimeMessage.class);
        
        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);
        
        mimeMessage.addRecipients(MimeMessage.RecipientType.TO, TO_ADDRESS);
        mimeMessage.setSubject(eq("이메일 제목 작성 [인증코드 {}]"), anyString());
        mimeMessage.setText(eq("이메일 내용 작성 : 인증코드 입니다. {}"), anyString());
        mimeMessage.setFrom(new InternetAddress("발송자@이메일.주소", "발송자 이름"));
        
        // -- 메일 발송
        willDoNothing().given(javaMailSender).send(mimeMessage);
        
        // -- redis 에 정보 저장
        EmailAuthDto emailAuthDto = EmailAuthDto.builder()
                .email(TO_ADDRESS)
                .code(anyString())
                .build();
        
        given(emailAuthConverter.toDto(TO_ADDRESS, anyString())).willReturn(emailAuthDto);
        given(redisTemplate.opsForSet()).willReturn(mock(SetOperations.class));
        given(redisTemplate.opsForSet().add(emailAuthDto.getEmail(), emailAuthDto.getCode())).willReturn(1L);
        given(redisTemplate.expire(eq(emailAuthDto.getEmail()), anyLong(), eq(TimeUnit.SECONDS))).willReturn(true);
        
        
        // when
        emailService.sendEmailAuthCode(TO_ADDRESS);
        
        
        // then
        InOrder order = inOrder(javaMailSender, redisTemplate);
        order.verify(javaMailSender).createMimeMessage();
        order.verify(javaMailSender).send(mimeMessage);
        order.verify(redisTemplate).opsForSet();
        order.verify(redisTemplate).expire(anyString(), anyLong(), any(TimeUnit.class));
        
        then(javaMailSender).should(times(1)).createMimeMessage();
        then(javaMailSender).should(times(1)).send(mimeMessage);
        then(redisTemplate.opsForSet()).should(times(1)).add(emailAuthDto.getEmail(), emailAuthDto.getCode());
        then(redisTemplate).should(times(1)).expire(eq(emailAuthDto.getEmail()), anyLong(), eq(TimeUnit.SECONDS));
    }
    
    @DisplayName("이메일 인증 후 redis 유저 정보에 email 저장")
    @Test
    void save_email_to_session_user() {
        // given
        SessionUserEmailUpdateDto sessionUserEmailUpdateDto = SessionUserEmailUpdateDto.builder()
                .id("session-id")
                .email("user@email.com")
                .emailVerification(true)
                .build();
        given(redisTemplate.delete(sessionUserEmailUpdateDto.getEmail())).willReturn(true);
        doNothing().when(sessionUserService).updateEmail(sessionUserEmailUpdateDto);
        
        // when
        emailService.deleteAndUpdateSessionUserEmail(sessionUserEmailUpdateDto);
        
        // then
        InOrder order = inOrder(redisTemplate, sessionUserService);
        order.verify(redisTemplate).delete(sessionUserEmailUpdateDto.getEmail());
        order.verify(sessionUserService).updateEmail(sessionUserEmailUpdateDto);
        
        then(redisTemplate).should(times(1)).delete(sessionUserEmailUpdateDto.getEmail());
        then(sessionUserService).should(times(1)).updateEmail(sessionUserEmailUpdateDto);
    }
}
