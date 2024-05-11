package kim.zhyun.serveruser.domain.signup.business;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.serveruser.domain.member.converter.UserConverter;
import kim.zhyun.serveruser.domain.member.repository.UserEntity;
import kim.zhyun.serveruser.domain.member.service.SessionUserService;
import kim.zhyun.serveruser.domain.signup.business.model.SessionUserEmailUpdateDto;
import kim.zhyun.serveruser.domain.signup.controller.model.EmailAuthCodeRequest;
import kim.zhyun.serveruser.domain.signup.controller.model.SignupRequest;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.EmailAuthDto;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.NicknameFindDto;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.NicknameUpdateDto;
import kim.zhyun.serveruser.domain.signup.converter.EmailAuthConverter;
import kim.zhyun.serveruser.domain.signup.converter.NicknameReserveConverter;
import kim.zhyun.serveruser.domain.signup.converter.SessionUserConverter;
import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
import kim.zhyun.serveruser.domain.signup.repository.SessionUser;
import kim.zhyun.serveruser.domain.signup.service.EmailService;
import kim.zhyun.serveruser.domain.signup.service.NicknameReserveService;
import kim.zhyun.serveruser.domain.signup.service.SignUpService;
import kim.zhyun.serveruser.utils.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static kim.zhyun.serveruser.common.message.ExceptionMessage.*;
import static kim.zhyun.serveruser.common.message.ResponseMessage.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@Slf4j
@DisplayName("sign up business test")
@ExtendWith(MockitoExtension.class)
class SignUpBusinessTest {
    
    @InjectMocks SignUpBusiness signUpBusiness;
    
    @Mock SignUpService signUpService;
    @Mock SessionUserService sessionUserService;
    @Mock NicknameReserveService nicknameReserveService;
    @Mock EmailService emailService;

    @Mock SessionUserConverter sessionUserConverter;
    @Mock NicknameReserveConverter nicknameReserveConverter;
    @Mock EmailAuthConverter emailAuthConverter;
    @Mock UserConverter userConverter;
    
    @Mock EmailUtil emailUtil;
    
    private final String SESSION_ID = "session-id";
    private final String TO_ADDRESS = "gimwlgus@gmail.com";
    
    
    
    @DisplayName("이메일 중복확인")
    @ParameterizedTest(name = "사용 가능: {0}")
    @ValueSource(booleans = {true, false})
    void emailDuplicateCheck(boolean availableEmail) {
        String requestEmail = "new@email.mail";
        String requestSessionId = "session-id-123523";
        
        given(signUpService.isAvailableEmail(requestEmail)).willReturn(availableEmail);
        
        // 사용 가능하면 `session user`에 email 저장
        SessionUserEmailUpdateDto sessionUserEmailUpdateDto = new SessionUserEmailUpdateDto();
        
        if (availableEmail) {
            sessionUserEmailUpdateDto = SessionUserEmailUpdateDto.builder()
                    .id(requestSessionId)
                    .email(requestEmail)
                    .emailVerification(false)
                    .build();
            
            given(sessionUserConverter.toEmailUpdateDto(requestSessionId, requestEmail, false)).willReturn(sessionUserEmailUpdateDto);
            willDoNothing().given(sessionUserService).updateEmail(sessionUserEmailUpdateDto);
        }
        
        
        // when
        String resultMessage = signUpBusiness.emailDuplicateCheck(requestEmail, requestSessionId);
        
        
        // then
        assertEquals(
                availableEmail
                        ? RESPONSE_SIGN_UP_AVAILABLE_EMAIL
                        : RESPONSE_SIGN_UP_UNAVAILABLE_EMAIL,
                resultMessage
        );
        
        then(signUpService).should(times(1)).isAvailableEmail(requestEmail);
        then(sessionUserService).should(availableEmail ? times(1) : never()).updateEmail(sessionUserEmailUpdateDto);
    }
    
    
    @DisplayName("닉네임 중복확인 ")
    @ParameterizedTest(name = "사용중: {0}, 예약중: {1}")
    @CsvSource({
            "true, true",
            "true, false",
            "false, true",
            "false, false",
    })
    void nicknameDuplicateCheck(boolean usedNickname, boolean reservedNickname) {
        String requestNickname  = "new nickname";
        String requestSessionId = "session-id-123523";
        
        given(signUpService.isUsedNickname(requestNickname)).willReturn(usedNickname);
        
        NicknameFindDto nicknameFindDto = new NicknameFindDto();
        NicknameUpdateDto nicknameUpdateDto = new NicknameUpdateDto();
        
        // 사용중이지 않은 닉네임
        if (!usedNickname) {
            nicknameFindDto = NicknameFindDto.builder()
                    .sessionId(requestSessionId)
                    .nickname(requestNickname)
                    .build();
            
            given(nicknameReserveConverter.toFindDto(requestSessionId, requestNickname)).willReturn(nicknameFindDto);
            given(nicknameReserveService.availableNickname(nicknameFindDto)).willReturn(!reservedNickname);
            
            // 예약되지 않은 닉네임
            if (!reservedNickname) {
                nicknameUpdateDto = NicknameUpdateDto.builder()
                        .id(requestSessionId)
                        .nickname(requestNickname)
                        .build();
                
                willDoNothing().given(nicknameReserveService).saveNickname(nicknameFindDto);
                given(nicknameReserveConverter.toUpdateDto(requestSessionId, requestNickname)).willReturn(nicknameUpdateDto);
                willDoNothing().given(sessionUserService).updateNickname(nicknameUpdateDto);
            }
        }
        
        
        // when
        String resultMessage = signUpBusiness.nicknameDuplicateCheck(requestNickname, requestSessionId);
        
        
        // then
        assertEquals(
                usedNickname
                        ? RESPONSE_SIGN_UP_UNAVAILABLE_NICKNAME
                        : reservedNickname
                                ? RESPONSE_SIGN_UP_UNAVAILABLE_NICKNAME
                                : RESPONSE_SIGN_UP_AVAILABLE_NICKNAME,
                resultMessage
        );
        
        then(signUpService)
                .should(times(1))
                .isUsedNickname(requestNickname);
        
        then(nicknameReserveConverter)
                .should(times( usedNickname ? 0 : 1))
                .toFindDto(requestSessionId, requestNickname);
        
        then(nicknameReserveService)
                .should(times( usedNickname ? 0 : 1))
                .availableNickname(nicknameFindDto);
        
        then(nicknameReserveService)
                .should(times( (usedNickname || reservedNickname) ? 0 : 1))
                .saveNickname(nicknameFindDto);
        
        then(nicknameReserveConverter)
                .should(times( (usedNickname || reservedNickname) ? 0 : 1))
                .toUpdateDto(requestSessionId, requestNickname);
        
        then(sessionUserService)
                .should(times( (usedNickname || reservedNickname) ? 0 : 1))
                .updateNickname(nicknameUpdateDto);
    }
    
    
    @DisplayName("이메일 전송 과정 ")
    @Test
    void send_email() throws MessagingException, UnsupportedEncodingException {
        // given
        EmailAuthCodeRequest emailAuthCodeRequest = EmailAuthCodeRequest.of(TO_ADDRESS);
        
        // -- 메일 중복 검사 확인
        willDoNothing().given(sessionUserService).emailDuplicateCheckWithThrow(SESSION_ID, emailAuthCodeRequest.getEmail());

        // -- 메일 발송
        String AUTH_CODE = "authcode";
        MimeMessage mimeMessage = mock(MimeMessage.class);
        
        given(emailUtil.getAuthCode()).willReturn(AUTH_CODE);
        given(emailUtil.createMessage(eq(emailAuthCodeRequest.getEmail()), anyString(), anyString(), anyString(), anyString())).willReturn(mimeMessage);
        willDoNothing().given(emailUtil).sendMail(mimeMessage);
        
        // -- 메일 인증 정보 저장
        EmailAuthDto emailAuthDto = EmailAuthDto.builder()
                .email(emailAuthCodeRequest.getEmail())
                .code(AUTH_CODE)
                .build();
        
        given(emailAuthConverter.toDto(emailAuthCodeRequest, AUTH_CODE)).willReturn(emailAuthDto);
        willDoNothing().given(emailService).saveEmailAuthInfo(emailAuthDto);
        
        
        // when
        String responseMessage = signUpBusiness.sendEmailAuthCode(SESSION_ID, emailAuthCodeRequest);
        
        
        // then
        assertEquals(RESPONSE_SEND_EMAIL_AUTH_CODE, responseMessage);
        
        InOrder order = inOrder(sessionUserService, emailUtil, emailAuthConverter, emailService);
        order.verify(sessionUserService, times(1)).emailDuplicateCheckWithThrow(SESSION_ID, emailAuthCodeRequest.getEmail());
        order.verify(emailUtil, times(1)).getAuthCode();
        order.verify(emailUtil, times(1)).createMessage(eq(emailAuthCodeRequest.getEmail()), anyString(), anyString(), anyString(), anyString());
        order.verify(emailUtil, times(1)).sendMail(mimeMessage);
        order.verify(emailAuthConverter, times(1)).toDto(emailAuthCodeRequest, AUTH_CODE);
        order.verify(emailService, times(1)).saveEmailAuthInfo(emailAuthDto);
    }
    
    
    @DisplayName("이메일 인증코드 검증 - 실패")
    @ParameterizedTest(name = "exist [메일유효: {0}, 인증코드: {1}]")
    @CsvSource({
            "true, false",
            "false, true",
            "false, false"
    })
    void verifyEmailAuthCode_fail(boolean existEmail, boolean existCode) {
        String requestSessionId = "session-id";
        String requestAuthCode = "email-auth-code-4312";
        
        // email 획득
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(requestSessionId)
                .email("new@email.mail")
                .emailVerification(true)
                .nickname("new user")
                .build();
        given(sessionUserService.findById(requestSessionId)).willReturn(sessionUser);
        String email = sessionUser.getEmail();
        
        // EmailAuthDto 획득
        EmailAuthDto requestInfo = EmailAuthDto.builder()
                .email(email)
                .code(requestAuthCode)
                .build();
        given(emailAuthConverter.toDto(email, requestAuthCode)).willReturn(requestInfo);
        
        // email 만료 여부
        given(emailService.existEmail(requestInfo)).willReturn(existEmail);
        
        // code 일치 여부
        if (existEmail) {
            given(emailService.existCode(requestInfo)).willReturn(existCode);
        }
        
        
        // when - then
        assertThrows(
                ApiException.class,
                () -> signUpBusiness.verifyEmailAuthCode(requestSessionId, requestAuthCode),
                !existEmail
                        ? EXCEPTION_VERIFY_EMAIL_AUTH_CODE_EXPIRED
                        : EXCEPTION_VERIFY_FAIL_EMAIL_AUTH_CODE
        );
        
        then(sessionUserConverter).should(times(0)).toEmailUpdateDto(requestSessionId, email, true);
        then(emailService).should(times(0)).deleteAndUpdateSessionUserEmail(new SessionUserEmailUpdateDto());
        then(sessionUserService).should(times(1)).findById(requestSessionId);
        then(emailAuthConverter).should(times(1)).toDto(email, requestAuthCode);
    }
    
    
    @DisplayName("이메일 인증코드 검증 - 성공")
    @ParameterizedTest(name = "exist [메일유효: {0}, 인증코드: {1}]")
    @CsvSource({
            "true, true"
    })
    void verifyEmailAuthCode_success(boolean existEmail, boolean existCode) {
        String requestSessionId = "session-id";
        String requestAuthCode = "email-auth-code-4312";
        
        // email 획득
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(requestSessionId)
                .email("new@email.mail")
                .emailVerification(true)
                .nickname("new user")
                .build();
        given(sessionUserService.findById(requestSessionId)).willReturn(sessionUser);
        String email = sessionUser.getEmail();
        
        // EmailAuthDto 획득
        EmailAuthDto requestInfo = EmailAuthDto.builder()
                .email(email)
                .code(requestAuthCode)
                .build();
        given(emailAuthConverter.toDto(email, requestAuthCode)).willReturn(requestInfo);
        
        // email 만료 여부
        given(emailService.existEmail(requestInfo)).willReturn(existEmail);
        
        // code 일치 여부
        given(emailService.existCode(requestInfo)).willReturn(existCode);
        
        // 메일 인증 성공시
        SessionUserEmailUpdateDto sessionUserEmailUpdateDto = SessionUserEmailUpdateDto.builder()
                .id(requestSessionId)
                .email(email)
                .emailVerification(true)
                .build();
        
        given(sessionUserConverter.toEmailUpdateDto(requestSessionId, email, true)).willReturn(sessionUserEmailUpdateDto);
        willDoNothing().given(emailService).deleteAndUpdateSessionUserEmail(sessionUserEmailUpdateDto);
        
        
        // when
        String responseMessage = signUpBusiness.verifyEmailAuthCode(requestSessionId, requestAuthCode);
        
        
        // then
        assertEquals(RESPONSE_VERIFY_EMAIL_AUTH_SUCCESS, responseMessage);
        
        then(sessionUserConverter).should(times(1)).toEmailUpdateDto(requestSessionId, email, true);
        then(emailService).should(times(1)).deleteAndUpdateSessionUserEmail(sessionUserEmailUpdateDto);
        then(sessionUserService).should(times(1)).findById(requestSessionId);
        then(emailAuthConverter).should(times(1)).toDto(email, requestAuthCode);
    }
    
    
    
    @DisplayName("신규 회원 등록 - 성공")
    @Test
    void saveMember_success() {
        String requestSessionId = "session-id";
        SignupRequest signupRequest = SignupRequest.of("new@email.mail", "new user", "password");

        // find session user by session id
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(requestSessionId)
                .email("new@email.mail")
                .emailVerification(true)
                .nickname("new user")
                .build();
        given(sessionUserService.findById(requestSessionId)).willReturn(sessionUser);
        
        // get role entity
        RoleEntity roleEntity = new RoleEntity();
        given(signUpService.getGrade(sessionUser.getEmail())).willReturn(roleEntity);
        
        // 신규 user 등록
        UserEntity newUserEntity = UserEntity.builder()
                .email(signupRequest.getEmail())
                .password(signupRequest.getPassword())
                .nickname(signupRequest.getNickname())
                .role(roleEntity)
                .withdrawal(false)
                .build();
        given(userConverter.toEntity(signupRequest, roleEntity)).willReturn(newUserEntity);

        UserEntity savedUserEntity = UserEntity.builder()
                .email(newUserEntity.getEmail())
                .nickname(newUserEntity.getNickname())
                .password(newUserEntity.getPassword())
                .role(newUserEntity.getRole())
                .withdrawal(newUserEntity.isWithdrawal())
                
                .id(100L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
        given(signUpService.saveUser(newUserEntity)).willReturn(savedUserEntity);
        
        // redis user info 업데이트
        willDoNothing().given(signUpService).jwtUserInfoUpdate(savedUserEntity);
        
        // 임시 저장 정보 삭제
        willDoNothing().given(sessionUserService).deleteById(requestSessionId);

        
        // when
        String resultMessage = signUpBusiness.saveMember(requestSessionId, signupRequest);
        
        
        // then
        assertEquals(RESPONSE_SUCCESS_FORMAT_SIGN_UP.formatted(signupRequest.getNickname()), resultMessage);
        
        then(sessionUserService).should(times(1)).findById(requestSessionId);
        then(signUpService).should(times(1)).getGrade(sessionUser.getEmail());
        then(userConverter).should(times(1)).toEntity(eq(signupRequest), any(RoleEntity.class));
        then(signUpService).should(times(1)).saveUser(any(UserEntity.class));
        then(signUpService).should(times(1)).jwtUserInfoUpdate(any(UserEntity.class));
        then(sessionUserService).should(times(1)).deleteById(requestSessionId);
    }
    
    
    @DisplayName("신규 회원 등록 - 실패")
    @ParameterizedTest(name = "{0}")
    @MethodSource
    void saveMember_fail(
            /*display name 표기용*/ String name,
                                 String requestSessionId, SessionUser sessionUser, SignupRequest signupRequest
    ) {
        given(sessionUserService.findById(requestSessionId)).willReturn(sessionUser);
        
        
        // when - then
        boolean emailException = sessionUser.getEmail() == null
                || !sessionUser.isEmailVerification()
                || !sessionUser.getEmail().equals(signupRequest.getEmail());
        
        assertThrows(
                ApiException.class,
                () -> signUpBusiness.saveMember(requestSessionId, signupRequest),
                emailException
                        ? EXCEPTION_REQUIRE_MAIL_DUPLICATE_CHECK
                        : EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK
        );
        
        then(sessionUserService).should(times(1)).findById(requestSessionId);
        
        then(signUpService).should(times(0)).getGrade(sessionUser.getEmail());
        then(userConverter).should(times(0)).toEntity(eq(signupRequest), any(RoleEntity.class));
        then(signUpService).should(times(0)).saveUser(any(UserEntity.class));
        then(signUpService).should(times(0)).jwtUserInfoUpdate(any(UserEntity.class));
        then(sessionUserService).should(times(0)).deleteById(requestSessionId);
    }
    static Stream<Arguments> saveMember_fail() {
        String requestSessionId = "session-id";
        
        String requestEmail = "new@email.mail";
        String requestNickname = "new user";
        return Stream.of(
                Arguments.of(
                        "메일 중복확인 안함",
                        requestSessionId,
                        SessionUser.builder()
                                .sessionId(requestSessionId)
                                .email(null)
                                .emailVerification(false)
                                .nickname(requestNickname)
                                .build(),
                        SignupRequest.of(requestEmail, requestNickname, "password")
                ),
                Arguments.of(
                        "메일 인증 안함",
                        requestSessionId,
                        SessionUser.builder()
                                .sessionId(requestSessionId)
                                .email(requestEmail)
                                .emailVerification(false)
                                .nickname(requestNickname)
                                .build(),
                        SignupRequest.of(requestEmail, requestNickname, "password")
                ),
                Arguments.of(
                        "메일 인증 후 다른 메일로 가입시도",
                        requestSessionId,
                        SessionUser.builder()
                                .sessionId(requestSessionId)
                                .email(requestEmail)
                                .emailVerification(true)
                                .nickname(requestNickname)
                                .build(),
                        SignupRequest.of("other_" + requestEmail, requestNickname, "password")
                ),
                Arguments.of(
                        "닉네임 중복확인 안함",
                        requestSessionId,
                        SessionUser.builder()
                                .sessionId(requestSessionId)
                                .email(requestEmail)
                                .emailVerification(true)
                                .nickname(null)
                                .build(),
                        SignupRequest.of(requestEmail, requestNickname, "password")
                ),
                Arguments.of(
                        "닉네임 중복확인 후 다른 메일로 가입시도",
                        requestSessionId,
                        SessionUser.builder()
                                .sessionId(requestSessionId)
                                .email(requestEmail)
                                .emailVerification(true)
                                .nickname(requestNickname)
                                .build(),
                        SignupRequest.of(requestEmail, "other_" + requestNickname, "password")
                )
        );
    }
    
}
