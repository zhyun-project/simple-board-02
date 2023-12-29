package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.advice.MailAuthException;
import kim.zhyun.serveruser.advice.SignUpException;
import kim.zhyun.serveruser.data.EmailAuthCodeRequest;
import kim.zhyun.serveruser.data.EmailAuthDto;
import kim.zhyun.serveruser.data.NicknameDto;
import kim.zhyun.serveruser.data.SignupRequest;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.data.type.RoleType;
import kim.zhyun.serveruser.repository.RoleRepository;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.service.EmailService;
import kim.zhyun.serveruser.service.NicknameReserveService;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

@Slf4j
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
    @Mock        RoleRepository roleRepository;
    
    @Nested
    @DisplayName("이메일 중복 확인")
    class DuplicateEmail {
        
        @DisplayName("사용 불가 - 사용중인 이메일")
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
        
        @DisplayName("사용 가능")
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
    }
    
    @DisplayName("닉네임 중복 확인")
    @Nested
    class NicknameDuplicate {
        
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
    }
    
    @DisplayName("인증 코드 메일 발송")
    @Nested
    class SendEmailAuthCode {
        
        @DisplayName("실패 : 중복검사 하지 않은 email")
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
        
        @DisplayName("성공")
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
    
    @DisplayName("인증 코드 검증")
    @Nested
    class VerifyEmailAuthCode {
        
        @DisplayName("성공")
        @Test
        void send_email_auth_code() {
            // given
            SessionUser sessionUser = SessionUser.builder()
                    .sessionId(SESSION_ID)
                    .email(EMAIL).build();
            String CODE = "AUTHC0D";
            EmailAuthDto requestInfo = EmailAuthDto.builder()
                    .email(EMAIL)
                    .code(CODE).build();
            
            when(sessionUserService.findById(SESSION_ID)).thenReturn(sessionUser);
            when(emailService.existEmail(requestInfo)).thenReturn(true);
            when(emailService.existCode(requestInfo)).thenReturn(true);
            
            // when
            signupService.verifyEmailAuthCode(SESSION_ID, CODE);
            
            // then
            verify(sessionUserService, times(1)).findById(SESSION_ID);
            verify(emailService, times(1)).existEmail(requestInfo);
            verify(emailService, times(1)).existCode(requestInfo);
            verify(emailService, times(1)).deleteAndUpdateSessionUserEmail(requestInfo, SESSION_ID);
        }
        
        @DisplayName("실패 - 인증 시간 만료")
        @Test
        void send_email_auth_code_fail_expired() {
            // given
            SessionUser sessionUser = SessionUser.builder()
                    .sessionId(SESSION_ID)
                    .email(EMAIL).build();
            String CODE = "AUTHC0D";
            EmailAuthDto requestInfo = EmailAuthDto.builder()
                    .email(EMAIL)
                    .code(CODE).build();
            
            when(sessionUserService.findById(SESSION_ID)).thenReturn(sessionUser);
            when(emailService.existEmail(requestInfo)).thenReturn(false);
            
            // when-then
            assertThrows(VERIFY_EMAIL_AUTH_CODE_EXPIRED,
                    MailAuthException.class,
                    () -> signupService.verifyEmailAuthCode(SESSION_ID, CODE));
            
            verify(sessionUserService, times(1)).findById(SESSION_ID);
            verify(emailService, times(1)).existEmail(requestInfo);
            verify(emailService, times(0)).existCode(requestInfo);
            verify(emailService, times(0)).deleteAndUpdateSessionUserEmail(requestInfo, SESSION_ID);
        }
        
        @DisplayName("실패 - 인증 코드 불일치")
        @Test
        void send_email_auth_code_fail_not_equals() {
            // given
            SessionUser sessionUser = SessionUser.builder()
                    .sessionId(SESSION_ID)
                    .email(EMAIL).build();
            String CODE = "AUTHC0D";
            EmailAuthDto requestInfo = EmailAuthDto.builder()
                    .email(EMAIL)
                    .code(CODE).build();
            
            when(sessionUserService.findById(SESSION_ID)).thenReturn(sessionUser);
            when(emailService.existEmail(requestInfo)).thenReturn(true);
            when(emailService.existCode(requestInfo)).thenReturn(false);
            
            // when-then
            assertThrows(VERIFY_FAIL_EMAIL_AUTH_CODE,
                    MailAuthException.class,
                    () -> signupService.verifyEmailAuthCode(SESSION_ID, CODE));
            
            verify(sessionUserService, times(1)).findById(SESSION_ID);
            verify(emailService, times(1)).existEmail(requestInfo);
            verify(emailService, times(1)).existCode(requestInfo);
            verify(emailService, times(0)).deleteAndUpdateSessionUserEmail(requestInfo, SESSION_ID);
        }
        
    }
    
    @DisplayName("회원가입")
    @Nested
    class SignUpSave {
        
        private final String EMAIL_CHANGED = "test@test.com";
        private final String NICKNAME_CHANGED = "닉네임변경";
        
        private final PasswordEncoder passwordEncoder;
        public SignUpSave(@Autowired PasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }
        
        @DisplayName("실패 - 이메일 중복확인 안함")
        @Test
        void fail_email_duplicate_pass() {
            // given
            SignupRequest signupRequest = SignupRequest.of(EMAIL_CHANGED, NICKNAME_CHANGED, "1234");
            SessionUser sessionUser = SessionUser.builder()
                    .sessionId(SESSION_ID)
                    .email(EMAIL)
                    .emailVerification(false)
                    .nickname(NICKNAME).build();
            
            when(sessionUserService.findById(SESSION_ID)).thenReturn(sessionUser);
            
            // when-then
            assertThrows(REQUIRE_MAIL_DUPLICATE_CHECK,
                    SignUpException.class,
                    () -> signupService.saveMember(SESSION_ID, signupRequest));
            
            verify(roleRepository, times(0)).findByRole(RoleType.MEMBER.name());
            verify(sessionUserService, times(0)).deleteById(SESSION_ID);
        }
        @DisplayName("실패 - 이메일 불일치")
        @Test
        void fail_email_changed() {
            // given
            SignupRequest signupRequest = SignupRequest.of(EMAIL_CHANGED, NICKNAME_CHANGED, "1234");
            SessionUser sessionUser = SessionUser.builder()
                    .sessionId(SESSION_ID)
                    .email(EMAIL)
                    .emailVerification(true)
                    .nickname(NICKNAME).build();
            
            when(sessionUserService.findById(SESSION_ID)).thenReturn(sessionUser);
            
            // when-then
            assertThrows(REQUIRE_MAIL_DUPLICATE_CHECK,
                    SignUpException.class,
                    () -> signupService.saveMember(SESSION_ID, signupRequest));
            
            verify(roleRepository, times(0)).findByRole(RoleType.MEMBER.name());
            verify(sessionUserService, times(0)).deleteById(SESSION_ID);
        }
        
        @DisplayName("실패 - 닉네임 불일치")
        @Test
        void fail_nickname_changed() {
            // given
            SignupRequest signupRequest = SignupRequest.of(EMAIL, NICKNAME_CHANGED, "1234");
            SessionUser sessionUser = SessionUser.builder()
                    .sessionId(SESSION_ID)
                    .email(EMAIL)
                    .emailVerification(true)
                    .nickname(NICKNAME).build();
            
            when(sessionUserService.findById(SESSION_ID)).thenReturn(sessionUser);
            
            // when-then
            assertThrows(REQUIRE_NICKNAME_DUPLICATE_CHECK,
                    SignUpException.class,
                    () -> signupService.saveMember(SESSION_ID, signupRequest));
            
            verify(roleRepository, times(0)).findByRole(RoleType.MEMBER.name());
            verify(sessionUserService, times(0)).deleteById(SESSION_ID);
        }
        
        @DisplayName("성공")
        @Test
        void success() {
            // given
            SignupRequest signupRequest = SignupRequest.of(EMAIL, NICKNAME, "1234");
            SessionUser sessionUser = SessionUser.builder()
                    .sessionId(SESSION_ID)
                    .email(EMAIL)
                    .emailVerification(true)
                    .nickname(NICKNAME).build();
            
            when(sessionUserService.findById(SESSION_ID)).thenReturn(sessionUser);
            
            // then
            signupService.saveMember(SESSION_ID, signupRequest);
            
            
            verify(roleRepository, times(1)).findByRole(RoleType.MEMBER.name());
            verify(sessionUserService, times(1)).deleteById(SESSION_ID);
        }
        
        @DisplayName("비밀번호 인코딩 확인")
        @Test
        void test_encoding_password() {
            // given
            String origin = "1234";
            
            // when
            String encoded = passwordEncoder.encode(origin);
            
            // then
            assertNotEquals(encoded, origin);
            log.info("인코딩 전 비밀번호 : {}, 인코딩 후 비밀번호 : {}", origin, encoded);
        }
        
    }
}
