package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.advice.MailAuthException;
import kim.zhyun.serveruser.data.EmailAuthCodeRequest;
import kim.zhyun.serveruser.data.NicknameDto;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.service.EmailService;
import kim.zhyun.serveruser.service.NicknameReserveService;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.REQUIRE_MAIL_DUPLICATE_CHECK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
class SignUpServiceImplTest {
    private final String NICKNAME   = "얼거스";
    private final String EMAIL      = "gimwlgus@gmail.com";
    private final String SESSION_ID = "6C62377C34168BB6DD496E8578447D78";
    
    @InjectMocks SignUpServiceImpl signupService;
    @Mock        UserRepository userRepository;
    @Mock        NicknameReserveService nicknameReserveService;
    @Mock        SessionUserService sessionUserService;
    @Mock        EmailService emailService;
    
    @DisplayName("이메일 사용 불가 - 사용중인 이메일")
    @Test
    void available_email_true() {
        // given
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(true);
        
        // when
        boolean availableEmail = signupService.availableEmail(EMAIL, SESSION_ID);
        
        // then
        verify(userRepository, times(1)).existsByEmailIgnoreCase(EMAIL);
        verify(sessionUserService, times(0)).findById(SESSION_ID);
        verify(sessionUserService, times(0)).save(null);
        
        assertThat(availableEmail).isFalse();
    }
    
    @DisplayName("이메일 사용 가능")
    @Test
    void available_email_false() {
        // given
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(SESSION_ID).build();
        
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(false);
        when(sessionUserService.findById(SESSION_ID)).thenReturn(sessionUser);

        // when
        boolean availableEmail = signupService.availableEmail(EMAIL, SESSION_ID);

        // then
        sessionUser.setEmail(EMAIL);
        
        verify(userRepository, times(1)).existsByEmailIgnoreCase(EMAIL);
        verify(sessionUserService, times(1)).findById(SESSION_ID);
        verify(sessionUserService, times(1)).save(sessionUser);
        
        assertThat(availableEmail).isTrue();
    }
    
    @DisplayName("사용 불가 - 사용중인 닉네임")
    @Test
    void available_nickname_false_cause_using() {
        // given
        when(userRepository.existsByNicknameIgnoreCase(NICKNAME)).thenReturn(true);
        
        // when
        boolean availableNickname = signupService.availableNickname(NICKNAME, SESSION_ID);
        
        // then
        verify(userRepository, times(1)).existsByNicknameIgnoreCase(NICKNAME);
        
        verify(nicknameReserveService, times(0)).existNickname(null);
        verify(nicknameReserveService, times(0)).saveNickname(null);
        verify(sessionUserService, times(0)).findById(SESSION_ID);
        verify(sessionUserService, times(0)).save(null);
        
        assertThat(availableNickname).isFalse();
    }
    
    @DisplayName("사용 불가 - 예약 된 닉네임")
    @Test
    void available_nickname_false_cause_reserved() {
        // given
        NicknameDto nicknameInfo = NicknameDto.builder()
                .nickname(NICKNAME)
                .sessionId(SESSION_ID).build();
        
        when(userRepository.existsByNicknameIgnoreCase(NICKNAME)).thenReturn(false);
        when(nicknameReserveService.existNickname(nicknameInfo)).thenReturn(true);
        
        // when
        boolean availableNickname = signupService.availableNickname(NICKNAME, SESSION_ID);
        
        // then
        verify(userRepository, times(1)).existsByNicknameIgnoreCase(NICKNAME);
        verify(nicknameReserveService, times(1)).existNickname(nicknameInfo);
        
        verify(nicknameReserveService, times(0)).saveNickname(nicknameInfo);
        verify(sessionUserService, times(0)).findById(SESSION_ID);
        verify(sessionUserService, times(0)).save(null);
        
        assertThat(availableNickname).isFalse();
    }
    
    @DisplayName("사용 가능")
    @Test
    void available_nickname_true() {
        // given
        NicknameDto nicknameInfo = NicknameDto.builder()
                .nickname(NICKNAME)
                .sessionId(SESSION_ID).build();
        
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(SESSION_ID).build();
        
        when(userRepository.existsByNicknameIgnoreCase(NICKNAME)).thenReturn(false);
        when(nicknameReserveService.existNickname(nicknameInfo)).thenReturn(false);
        when(sessionUserService.findById(SESSION_ID)).thenReturn(sessionUser);
        
        // when
        boolean availableNickname = signupService.availableNickname(NICKNAME, SESSION_ID);
        
        // then
        sessionUser.setNickname(NICKNAME);
        
        verify(userRepository, times(1)).existsByNicknameIgnoreCase(NICKNAME);
        verify(nicknameReserveService, times(1)).existNickname(nicknameInfo);
        verify(nicknameReserveService, times(1)).saveNickname(nicknameInfo);
        verify(sessionUserService, times(1)).findById(SESSION_ID);
        verify(sessionUserService, times(1)).save(sessionUser);
        
        assertThat(availableNickname).isTrue();
    }
    
    
    @DisplayName("인증 코드 메일 발송 - 실패 : 중복검사 하지 않은 email")
    @Test
    void send_email_auth_code_fail() {
        // given
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(SESSION_ID).build();
        EmailAuthCodeRequest requestInfo = EmailAuthCodeRequest.of(EMAIL);
        
        // when
        when(sessionUserService.findById(SESSION_ID)).thenReturn(sessionUser);
        
        // then
        assertThrows(REQUIRE_MAIL_DUPLICATE_CHECK,
                MailAuthException.class,
                () -> signupService.sendEmailAuthCode(SESSION_ID, requestInfo));
        
        verify(sessionUserService, times(1)).findById(SESSION_ID);
        verify(emailService, times(0)).sendEmailAuthCode(null);
    }
    
    @DisplayName("인증 코드 메일 발송 - 성공")
    @Test
    void send_email_auth_code() {
        // given
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(SESSION_ID)
                .email(EMAIL).build();
        EmailAuthCodeRequest requestInfo = EmailAuthCodeRequest.of(EMAIL);
        
        // when
        when(sessionUserService.findById(SESSION_ID)).thenReturn(sessionUser);
        
        signupService.sendEmailAuthCode(SESSION_ID, requestInfo);
        
        // then
        verify(sessionUserService, times(1)).findById(SESSION_ID);
        verify(emailService, times(1)).sendEmailAuthCode(requestInfo.getEmail());
    }
    
}
