package kim.zhyun.serveruser.domain.signup.controller;

import kim.zhyun.jwt.common.model.ApiResponse;
import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.serveruser.common.message.ExceptionMessage;
import kim.zhyun.serveruser.common.message.ResponseMessage;
import kim.zhyun.serveruser.domain.signup.business.SignUpBusiness;
import kim.zhyun.serveruser.domain.signup.controller.model.EmailAuthCodeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CheckApiControllerTest {
    
    @InjectMocks CheckApiController checkApiController;
    @Mock SignUpBusiness signUpBusiness;
    
    
    
    @DisplayName("중복 확인 실패 - 이메일 = null, 닉네임 = null")
    @Test
    void duplicateCheck_fail() {
        // given
        String email = null;
        String nickname = null;
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        
        
        // when
        ResponseEntity<ApiResponse<Void>> responseEntity = checkApiController.duplicateCheck(request, email, nickname);
        
        
        // then
        assertEquals(responseEntity.getStatusCode(),        HttpStatusCode.valueOf(HttpStatus.OK.value()));
        assertEquals(responseEntity.getBody().getStatus(),  false);
        assertEquals(responseEntity.getBody().getMessage(), ResponseMessage.RESPONSE_SIGN_UP_CHECK_VALUE_IS_EMPTY);
    }
    
    
    @DisplayName("이메일 중복 확인 성공")
    @ParameterizedTest
    @CsvSource({
            "email.com@address.mail",
            "e-mail@email.mail",
            "e+mail@email.mail",
            "e_mail@email.mail",
            "e.mail@email.mail",
            "email@email.mail",
            
            "1email.com@address.ma.il",
            "1e-mail@email.e.mail",
            "1e+mail@email.mail.ail",
            "1e_mail@email.mail.ail",
            "1e.mail@email.mail.ail",
            "1email@email.mail.ail"
    })
    void duplicateCheck_email_success(String email) {
        // given
        String nickname = null;

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());

        String responseMessage = ResponseMessage.RESPONSE_SIGN_UP_AVAILABLE_EMAIL;
        
        given(signUpBusiness.emailDuplicateCheck(email, request.getSession().getId())).willReturn(responseMessage);
        
        
        // when
        ResponseEntity<ApiResponse<Void>> responseEntity = checkApiController.duplicateCheck(request, email, nickname);
        
        
        // then
        assertEquals(responseEntity.getStatusCode(),        HttpStatusCode.valueOf(HttpStatus.OK.value()));
        assertEquals(responseEntity.getBody().getStatus(),  true);
        assertEquals(responseEntity.getBody().getMessage(), responseMessage);
    }
    
    @DisplayName("이메일 중복 확인 실패 - 규격 안맞음")
    @ParameterizedTest
    @ValueSource(strings = {
            "email",
            "email.com@com",
            "email@email",
            "email@email.",
            "-email@wemilamasd.com",
            ".email@wemilamasd.com",
            "_email@wemilamasd.com",
            "+email@wemilamasd.com",
            "e!mail@wemilamasd.com",
            "e#mail@wemilamasd.com",
            "e~mail@wemilamasd.com",
            "e%mail@wemilamasd.com",
            "e^mail@wemilamasd.com",
            "e$mail@wemilamasd.com",
            "e&mail@wemilamasd.com",
            "e*mail@wemilamasd.com",
            "e mail@wemilamasd.com",
            "e　mail@wemilamasd.com",
            "email@wemila@masd.com",
    })
    void duplicateCheck_email_fail(String email) {
        // given
        String nickname = null;
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        
        String responseMessage = ResponseMessage.RESPONSE_SIGN_UP_UNAVAILABLE_EMAIL;
        
        given(signUpBusiness.emailDuplicateCheck(email, request.getSession().getId())).willReturn(responseMessage);
        
        
        // when
        ResponseEntity<ApiResponse<Void>> responseEntity = checkApiController.duplicateCheck(request, email, nickname);
        
        
        // then
        assertEquals(responseEntity.getStatusCode(),        HttpStatusCode.valueOf(HttpStatus.OK.value()));
        assertEquals(responseEntity.getBody().getStatus(),  false);
        assertEquals(responseEntity.getBody().getMessage(), responseMessage);
    }
    
    
    
    @DisplayName("닉네임 중복 확인 성공")
    @ParameterizedTest
    @ValueSource(strings = {
            "a    a",
            "aa   a",
            "aaa  a",
            "aaaa a",
            "aaaaaa",
            "abcdef",
            "일이삼사오육",
            "일이🦆 사오",
            "일이🦆사오육",
            "🙆🙆🙆🙆🙆🙆",
    })
    void duplicateCheck_nickname_success(String nickname) {
        // given
        String email = null;
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        
        String responseMessage = ResponseMessage.RESPONSE_SIGN_UP_AVAILABLE_NICKNAME;
        
        given(signUpBusiness.nicknameDuplicateCheck(nickname, request.getSession().getId())).willReturn(responseMessage);
        
        
        // when
        ResponseEntity<ApiResponse<Void>> responseEntity = checkApiController.duplicateCheck(request, email, nickname);
        
        
        // then
        assertEquals(responseEntity.getStatusCode(),        HttpStatusCode.valueOf(HttpStatus.OK.value()));
        assertEquals(responseEntity.getBody().getStatus(),  true);
        assertEquals(responseEntity.getBody().getMessage(), responseMessage);
    }
    
    @DisplayName("닉네임 중복 확인 실패 - 규격 안맞음")
    @ParameterizedTest
    @ValueSource(strings = {
            "      ",
            "   a  ",
            "  a   ",
            " a    ",
            "a    ",
            " bcdef",
            "abcde ",
            "abcdef ",
            " abcdef",
            "일이삼사오육칠",
            "일이🦆 사오육",
            " 일이🦆사오육",
            "일이🦆사오육 ",
            "🙆🙆🙆🙆🙆🙆 ",
            " 🙆🙆🙆🙆🙆🙆",
            "🙆🙆🙆 🙆🙆🙆",
    })
    void duplicateCheck_nickname_fail(String nickname) {
        // given
        String email = null;
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        
        String responseMessage = ExceptionMessage.EXCEPTION_VALID_NICKNAME_FORMAT;
        
        given(signUpBusiness.nicknameDuplicateCheck(nickname, request.getSession().getId())).willReturn(responseMessage);
        
        
        // when
        ResponseEntity<ApiResponse<Void>> responseEntity = checkApiController.duplicateCheck(request, email, nickname);
        
        
        // then
        assertEquals(responseEntity.getStatusCode(),        HttpStatusCode.valueOf(HttpStatus.OK.value()));
        assertEquals(responseEntity.getBody().getStatus(),  false);
        assertEquals(responseEntity.getBody().getMessage(), responseMessage);
    }
    
    @DisplayName("닉네임 중복 확인 실패 - 다른 회원이 사용하고 있거나 다른 사람이 먼저 예약한 경우")
    @Test
    void duplicateCheck_nickname_fail_() {
        // given
        String nickname = "used";
        String email = null;
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        
        String responseMessage = ResponseMessage.RESPONSE_SIGN_UP_UNAVAILABLE_NICKNAME;
        
        given(signUpBusiness.nicknameDuplicateCheck(nickname, request.getSession().getId())).willReturn(responseMessage);
        
        
        // when
        ResponseEntity<ApiResponse<Void>> responseEntity = checkApiController.duplicateCheck(request, email, nickname);
        
        
        // then
        assertEquals(responseEntity.getStatusCode(),        HttpStatusCode.valueOf(HttpStatus.OK.value()));
        assertEquals(responseEntity.getBody().getStatus(),  false);
        assertEquals(responseEntity.getBody().getMessage(), responseMessage);
    }
    
    
    @DisplayName("이메일로 인증코드 전송 - 성공")
    @Test
    void sendEmail_success() {
        // given
        EmailAuthCodeRequest emailAuthCodeRequest = EmailAuthCodeRequest.of("new@email.mail");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        
        String responseMessage = ResponseMessage.RESPONSE_SEND_EMAIL_AUTH_CODE;
        
        given(signUpBusiness.sendEmailAuthCode(request.getSession().getId(), emailAuthCodeRequest)).willReturn(responseMessage);
        
        
        // when
        ResponseEntity<ApiResponse<Void>> responseEntity = checkApiController.sendEmail(request, emailAuthCodeRequest);
        
        
        // then
        assertEquals(responseEntity.getStatusCode(),        HttpStatusCode.valueOf(HttpStatus.OK.value()));
        assertEquals(responseEntity.getBody().getStatus(),  true);
        assertEquals(responseEntity.getBody().getMessage(), responseMessage);
    }
    
    
    @DisplayName("이메일로 인증코드 전송 - 실패: email 중복검사 안함")
    @Test
    void sendEmail_fail_cause_email_not_duplicate_check() {
        // given
        EmailAuthCodeRequest emailAuthCodeRequest = EmailAuthCodeRequest.of("new@email.mail");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        
        String responseMessage = ExceptionMessage.EXCEPTION_REQUIRE_MAIL_DUPLICATE_CHECK;
        
        given(signUpBusiness.sendEmailAuthCode(request.getSession().getId(), emailAuthCodeRequest)).willThrow(new ApiException(responseMessage));
        
        
        // when - then
        assertThrows(
                ApiException.class,
                () -> checkApiController.sendEmail(request, emailAuthCodeRequest),
                responseMessage
        );
    }
    
    @DisplayName("이메일로 인증코드 전송 - 실패: java mail sender error")
    @Test
    void sendEmail_fail_cause_java_mail_sender() {
        // given
        EmailAuthCodeRequest emailAuthCodeRequest = EmailAuthCodeRequest.of("new@email.mail");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        
        String responseMessage = ExceptionMessage.EXCEPTION_MAIL_SEND_FAIL;
        
        given(signUpBusiness.sendEmailAuthCode(request.getSession().getId(), emailAuthCodeRequest)).willThrow(new RuntimeException(responseMessage));
        
        
        // when - then
        assertThrows(
                RuntimeException.class,
                () -> checkApiController.sendEmail(request, emailAuthCodeRequest),
                responseMessage
        );
    }
    
    
    @DisplayName("메일 인증코드 검증 - 성공")
    @Test
    void authEmailCode_success() {
        // given
        String code = "auth-code";
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        
        String responseMessage = ResponseMessage.RESPONSE_VERIFY_EMAIL_AUTH_SUCCESS;
        
        given(signUpBusiness.verifyEmailAuthCode(request.getSession().getId(), code)).willReturn(responseMessage);
        
        
        // when
        ResponseEntity<ApiResponse<Void>> responseEntity = checkApiController.authEmailCode(request, code);
        
        
        // then
        assertEquals(responseEntity.getStatusCode(),        HttpStatusCode.valueOf(HttpStatus.OK.value()));
        assertEquals(responseEntity.getBody().getStatus(),  true);
        assertEquals(responseEntity.getBody().getMessage(), responseMessage);
    }
    
    @DisplayName("메일 인증코드 검증 - 실패: 인증 코드 만료 혹은 인증 코드 불일치")
    @ParameterizedTest
    @ValueSource(strings = {
            ExceptionMessage.EXCEPTION_VERIFY_EMAIL_AUTH_CODE_EXPIRED,
            ExceptionMessage.EXCEPTION_VERIFY_FAIL_EMAIL_AUTH_CODE
    })
    void authEmailCode_fail(String responseMessage) {
        // given
        String code = "auth-code";
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        
        given(signUpBusiness.verifyEmailAuthCode(request.getSession().getId(), code)).willThrow(new ApiException(responseMessage));
        
        
        // when - then
        assertThrows(
                ApiException.class,
                () -> checkApiController.authEmailCode(request, code),
                responseMessage
        );
    }
    
}