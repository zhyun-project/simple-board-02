package kim.zhyun.serveruser.domain.signup.controller;

import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.jwt.exception.MailSenderException;
import kim.zhyun.jwt.exception.message.CommonExceptionMessage;
import kim.zhyun.jwt.filter.JwtFilter;
import kim.zhyun.serveruser.common.message.ExceptionMessage;
import kim.zhyun.serveruser.common.message.ResponseMessage;
import kim.zhyun.serveruser.config.TestSecurityConfig;
import kim.zhyun.serveruser.domain.signup.business.SignUpBusiness;
import kim.zhyun.serveruser.domain.signup.controller.model.EmailAuthCodeRequest;
import kim.zhyun.serveruser.filter.AuthenticationFilter;
import kim.zhyun.serveruser.filter.SessionCheckFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@WebMvcTest(
        controllers = CheckApiController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        AuthenticationFilter.class,
                        JwtFilter.class,
                        SessionCheckFilter.class
                }
        )
)
class CheckApiControllerTest {
    
    @MockBean SignUpBusiness signUpBusiness;
    
    @Autowired MockMvc mvc;
    
    ObjectMapper objectMapper = new ObjectMapper();


    @DisplayName("이메일 중복 확인 실패 = null")
    @Test
    void emailDuplicateCheck_fail() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();


        // when - then
        mvc.perform(
                        get("/check/duplicate-email")
                                .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(ExceptionMessage.EXCEPTION_EMAIL_FIELD_IS_NULL))
                .andDo(print());
    }

    @DisplayName("닉네임 중복 확인 실패 = null")
    @Test
    void nicknameDuplicateCheck_fail() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();


        // when - then
        mvc.perform(
                        get("/check/duplicate-nickname")
                                .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(ExceptionMessage.EXCEPTION_NICKNAME_FIELD_IS_NULL))
                .andDo(print());
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
    void duplicateCheck_email_success(String email) throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        
        String responseMessage = ResponseMessage.RESPONSE_SIGN_UP_AVAILABLE_EMAIL;
        
        given(signUpBusiness.emailDuplicateCheck(email, session.getId())).willReturn(responseMessage);
        
        
        // when - then
        mvc.perform(
                        get("/check/duplicate-email")
                                .session(session)
                                .param("email", email)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
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
    void duplicateCheck_email_fail(String email) throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        
        String responseMessage = CommonExceptionMessage.EXCEPTION_VALID_FORMAT;
        
        given(signUpBusiness.emailDuplicateCheck(email, session.getId())).willReturn(responseMessage);
        
        
        // when - then
        String validExceptionMessage = ExceptionMessage.EXCEPTION_VALID_EMAIL_FORMAT;
        
        mvc.perform(
                        get("/check/duplicate-email")
                                .session(session)
                                .param("email", email)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andExpect(jsonPath("$.result[0].field").value("email"))
                .andExpect(jsonPath("$.result[0].message").value(validExceptionMessage))
                .andDo(print());
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
    void duplicateCheck_nickname_success(String nickname) throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        
        String responseMessage = ResponseMessage.RESPONSE_SIGN_UP_AVAILABLE_NICKNAME;
        
        given(signUpBusiness.nicknameDuplicateCheck(nickname, session.getId())).willReturn(responseMessage);
        
        
        // when - then
        mvc.perform(
                        get("/check/duplicate-nickname")
                                .session(session)
                                .param("nickname", nickname)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
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
    void duplicateCheck_nickname_fail(String nickname) throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        
        String responseMessage = CommonExceptionMessage.EXCEPTION_VALID_FORMAT;
        
        given(signUpBusiness.nicknameDuplicateCheck(nickname, session.getId())).willReturn(responseMessage);
        
        
        // when - then
        String validExceptionMessage = ExceptionMessage.EXCEPTION_VALID_NICKNAME_FORMAT;
        
        mvc.perform(
                        get("/check/duplicate-nickname")
                                .session(session)
                                .param("nickname", nickname)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andExpect(jsonPath("$.result[0].field").value("nickname"))
                .andExpect(jsonPath("$.result[0].message").value(validExceptionMessage))
                .andDo(print());
    }
    
    @DisplayName("닉네임 중복 확인 실패 - 다른 회원이 사용하고 있거나 다른 사람이 먼저 예약한 경우")
    @Test
    void duplicateCheck_nickname_fail_() throws Exception {
        // given
        String nickname = "used";
        
        MockHttpSession session = new MockHttpSession();
        
        String responseMessage = ResponseMessage.RESPONSE_SIGN_UP_UNAVAILABLE_NICKNAME;
        
        given(signUpBusiness.nicknameDuplicateCheck(nickname, session.getId())).willReturn(responseMessage);
        
        
        // when - then
        mvc.perform(
                        get("/check/duplicate-nickname")
                                .session(session)
                                .param("nickname", nickname)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    
    @DisplayName("이메일로 인증코드 전송 - 성공")
    @Test
    void sendEmail_success() throws Exception {
        // given
        EmailAuthCodeRequest emailAuthCodeRequest = EmailAuthCodeRequest.of("new@email.mail");
        
        MockHttpSession session = new MockHttpSession();
        
        String responseMessage = ResponseMessage.RESPONSE_SEND_EMAIL_AUTH_CODE;
        
        given(signUpBusiness.sendEmailAuthCode(session.getId(), emailAuthCodeRequest)).willReturn(responseMessage);
        
        
        // when - then
        mvc.perform(
                        post("/check/auth")
                                .session(session)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(emailAuthCodeRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    
    @DisplayName("이메일로 인증코드 전송 - 실패: email 중복검사 안함")
    @Test
    void sendEmail_fail_cause_email_not_duplicate_check() throws Exception {
        // given
        EmailAuthCodeRequest emailAuthCodeRequest = EmailAuthCodeRequest.of("new@email.mail");
        
        MockHttpSession session = new MockHttpSession();
        
        String responseMessage = ExceptionMessage.EXCEPTION_REQUIRE_MAIL_DUPLICATE_CHECK;
        
        given(signUpBusiness.sendEmailAuthCode(session.getId(), emailAuthCodeRequest)).willThrow(new ApiException(responseMessage));
        
        
        // when - then
        mvc.perform(
                        post("/check/auth")
                                .session(session)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(emailAuthCodeRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    @DisplayName("이메일로 인증코드 전송 - 실패: java mail sender error")
    @Test
    void sendEmail_fail_cause_java_mail_sender() throws Exception {
        // given
        EmailAuthCodeRequest emailAuthCodeRequest = EmailAuthCodeRequest.of("new@email.mail");
        
        MockHttpSession session = new MockHttpSession();
        
        String responseMessage = ExceptionMessage.EXCEPTION_MAIL_SEND_FAIL;
        
        given(signUpBusiness.sendEmailAuthCode(session.getId(), emailAuthCodeRequest)).willThrow(new MailSenderException(responseMessage));
        
        
        // when - then
        mvc.perform(
                        post("/check/auth")
                                .session(session)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(emailAuthCodeRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    
    @DisplayName("메일 인증코드 검증 - 성공")
    @Test
    void authEmailCode_success() throws Exception {
        // given
        String code = "auth-code";
        
        MockHttpSession session = new MockHttpSession();
        
        String responseMessage = ResponseMessage.RESPONSE_VERIFY_EMAIL_AUTH_SUCCESS;
        
        given(signUpBusiness.verifyEmailAuthCode(session.getId(), code)).willReturn(responseMessage);
        
        
        // when - then
        mvc.perform(
                        get("/check/auth")
                                .session(session)
                                .param("code", code)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    @DisplayName("메일 인증코드 검증 - 실패: 인증 코드 만료 혹은 인증 코드 불일치")
    @ParameterizedTest
    @ValueSource(strings = {
            ExceptionMessage.EXCEPTION_VERIFY_EMAIL_AUTH_CODE_EXPIRED,
            ExceptionMessage.EXCEPTION_VERIFY_FAIL_EMAIL_AUTH_CODE
    })
    void authEmailCode_fail(String responseMessage) throws Exception {
        // given
        String code = "auth-code";
        
        MockHttpSession session = new MockHttpSession();
        
        given(signUpBusiness.verifyEmailAuthCode(session.getId(), code)).willThrow(new ApiException(responseMessage));
        
        
        // when - then
        mvc.perform(
                        get("/check/auth")
                                .session(session)
                                .param("code", code)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
}