package kim.zhyun.serveruser.domain.signup.service;

import kim.zhyun.serveruser.domain.member.service.SessionUserService;
import kim.zhyun.serveruser.domain.signup.business.model.SessionUserEmailUpdateDto;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.EmailAuthDto;
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
    
    @DisplayName("이메일 인증 정보 redis 저장")
    @Test
    void save_email_auth() {
        // given
        EmailAuthDto emailAuthDto = EmailAuthDto.builder()
                .code(EMAIL_AUTH_CODE)
                .email(TO_ADDRESS)
                .build();
        
        SetOperations setOperations = mock(SetOperations.class);
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        
        given(redisTemplate.opsForSet().add(emailAuthDto.getEmail(), emailAuthDto.getCode())).willReturn(1L);
        given(redisTemplate.expire(eq(emailAuthDto.getEmail()), anyLong(), eq(TimeUnit.SECONDS))).willReturn(true);
        
        
        // when
        emailService.saveEmailAuthInfo(emailAuthDto);
        
        
        // then
        InOrder order = inOrder(redisTemplate, setOperations);
        order.verify(setOperations, times(1)).add(emailAuthDto.getEmail(), emailAuthDto.getCode());
        order.verify(redisTemplate, times(1)).expire(eq(emailAuthDto.getEmail()), anyLong(), any(TimeUnit.class));
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
        order.verify(redisTemplate, times(1)).delete(sessionUserEmailUpdateDto.getEmail());
        order.verify(sessionUserService, times(1)).updateEmail(sessionUserEmailUpdateDto);
    }
}
