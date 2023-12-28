package kim.zhyun.serveruser.controller;

import kim.zhyun.serveruser.advice.MailAuthException;
import kim.zhyun.serveruser.data.EmailAuthCodeRequest;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.service.SignUpService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.REQUIRED_REQUEST_BODY;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.REQUIRE_MAIL_DUPLICATE_CHECK;
import static kim.zhyun.serveruser.data.message.ResponseMessage.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
@ExtendWith(RedisTestContainer.class)
@AutoConfigureMockMvc
@SpringBootTest
class CheckControllerTest {
    private final String NICKNAME = "얼거스";
    private final String EMAIL = "gimwlgus@gmail.com";
    
    private final String EMAIL_VALID_EXCEPTION_MESSAGE = "올바른 이메일 주소를 입력해주세요.";
    
    @MockBean
    private SignUpService signupService;
    
    private final MockMvc mvc;
    private final ObjectMapper mapper;
    public CheckControllerTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
        this.mapper = new ObjectMapper();
    }
    
    @DisplayName("중복 확인 - 빈 값 입력")
    @Test
    void duplicate_check_empty() throws Exception {
        mvc.perform(get("/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(false))
                .andExpect(jsonPath("message").value(SIGN_UP_CHECK_VALUE_IS_EMPTY.getMessage()))
                .andDo(print());
        
        verify(signupService, times(0)).availableEmail(anyString(), anyString());
        verify(signupService, times(0)).availableNickname(anyString(), anyString());
    }
    
    @DisplayName("닉네임 중복 확인 - 사용 가능")
    @Test
    void duplicate_check_nickname() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String sessionId = session.getId();
        when(signupService.availableNickname(NICKNAME, sessionId)).thenReturn(true);
        
        mvc.perform(get("/check").param("nickname", NICKNAME).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(true))
                .andExpect(jsonPath("message").value(SIGN_UP_AVAILABLE_NICKNAME.getMessage()))
                .andDo(print());
    }
    
    @DisplayName("닉네임 중복 확인 - 사용 불가")
    @Test
    void duplicate_check_nickname_using() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String sessionId = session.getId();
        when(signupService.availableNickname(NICKNAME, sessionId)).thenReturn(false);
        
        mvc.perform(get("/check").param("nickname", NICKNAME)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(false))
                .andExpect(jsonPath("message").value(SIGN_UP_UNAVAILABLE_NICKNAME.getMessage()))
                .andDo(print());
        
        verify(signupService, times(0)).availableEmail(EMAIL, sessionId);
        verify(signupService, times(1)).availableNickname(NICKNAME, sessionId);
    }
    
    @DisplayName("닉네임 중복 확인 - 유효한 형식이 아님")
    @Test
    void duplicate_check_nickname_valid_exception() throws Exception {
        mvc.perform(get("/check").param("nickname", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(VALID_EXCEPTION.getMessage()))
                .andExpect(jsonPath("$.result.[0].field").value("nickname"))
                .andExpect(jsonPath("$.result.[0].message").value("1글자 이상, 6글자 이하로 입력해주세요."))
                .andDo(print());
        
        verify(signupService, times(0)).availableEmail(anyString(), anyString());
        verify(signupService, times(0)).availableNickname(anyString(), anyString());
    }
    
    @DisplayName("이메일 중복 확인 - 사용 가능")
    @Test
    void duplicate_check_email() throws Exception {
        when(signupService.availableEmail(anyString(), anyString())).thenReturn(true);
        
        mvc.perform(get("/check").param("email", EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(true))
                .andExpect(jsonPath("message").value(SIGN_UP_AVAILABLE_EMAIL.getMessage()))
                .andDo(print());
        
        verify(signupService, times(1)).availableEmail(anyString(), anyString());
        verify(signupService, times(0)).availableNickname(anyString(), anyString());
    }
    
    @DisplayName("이메일 중복 확인 - 사용 불가")
    @Test
    void duplicate_check_email_fail() throws Exception {
        when(signupService.availableEmail(anyString(), anyString())).thenReturn(false);
        
        mvc.perform(get("/check").param("email", EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(false))
                .andExpect(jsonPath("message").value(SIGN_UP_UNAVAILABLE_EMAIL.getMessage()))
                .andDo(print());
        
        verify(signupService, times(1)).availableEmail(anyString(), anyString());
        verify(signupService, times(0)).availableNickname(anyString(), anyString());
    }
    
    @DisplayName("이메일 중복 확인 - 유효한 형식이 아님 1. 공백 입력")
    @Test
    void duplicate_check_email_valid_exception_blank() throws Exception {
        mvc.perform(get("/check").param("email", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(VALID_EXCEPTION.getMessage()))
                .andExpect(jsonPath("$.result.[0].field").value("email"))
                .andExpect(jsonPath("$.result.[0].message").value(EMAIL_VALID_EXCEPTION_MESSAGE))
                .andDo(print());
        
        verify(signupService, times(0)).availableEmail(anyString(), anyString());
        verify(signupService, times(0)).availableNickname(anyString(), anyString());
    }
    
    @DisplayName("이메일 중복 확인 - 유효한 형식이 아님 1. 형식 오류")
    @Test
    void duplicate_check_email_valid_exception_format() throws Exception {
        mvc.perform(get("/check").param("email", "오호@."))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(VALID_EXCEPTION.getMessage()))
                .andExpect(jsonPath("$.result.[0].field").value("email"))
                .andExpect(jsonPath("$.result.[0].message").value(EMAIL_VALID_EXCEPTION_MESSAGE))
                .andDo(print());
        
        verify(signupService, times(0)).availableEmail(anyString(), anyString());
        verify(signupService, times(0)).availableNickname(anyString(), anyString());
    }
    
    @DisplayName("인증 코드 메일 발송 - request body is empty")
    @Test
    void send_email_fail_not_input_email() throws Exception {
        // when-then
        mvc.perform(post("/check/auth")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(REQUIRED_REQUEST_BODY.getMessage()))
                .andDo(print());
        
        verify(signupService, times(0)).sendEmailAuthCode("", null);
    }
    
    @DisplayName("인증 코드 메일 발송 - 이메일 입력 안됨")
    @Test
    void send_email_fail_email_valid_exception_null() throws Exception {
        // given
        EmailAuthCodeRequest given = EmailAuthCodeRequest.of(null);
        
        // when-then
        mvc.perform(post("/check/auth")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(given)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(VALID_EXCEPTION.getMessage()))
                .andExpect(jsonPath("$.result.[0].field").value("email"))
                .andExpect(jsonPath("$.result.[0].message").value(EMAIL_VALID_EXCEPTION_MESSAGE))
                .andDo(print());
        
        verify(signupService, times(0)).sendEmailAuthCode("", given);
    }
    
    @DisplayName("인증 코드 메일 발송 - 이메일 형식 오류")
    @Test
    void send_email_fail_email_valid_exception() throws Exception {
        // given
        EmailAuthCodeRequest given = EmailAuthCodeRequest.of("test@asd");
        
        // when-then
        mvc.perform(post("/check/auth")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(given)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(VALID_EXCEPTION.getMessage()))
                .andExpect(jsonPath("$.result.[0].field").value("email"))
                .andExpect(jsonPath("$.result.[0].message").value(EMAIL_VALID_EXCEPTION_MESSAGE))
                .andDo(print());
        
        verify(signupService, times(0)).sendEmailAuthCode("", given);
    }
    
    @DisplayName("인증 코드 메일 발송 - fail : 이메일 중복검사 안함")
    @Test
    void send_email_fail_email_duplicate_check_pass() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        String sessionId = session.getId();
        
        EmailAuthCodeRequest given = EmailAuthCodeRequest.of(EMAIL);
        
        doThrow(new MailAuthException(REQUIRE_MAIL_DUPLICATE_CHECK))
                .when(signupService).sendEmailAuthCode(sessionId, given);
        
        // when-then
        mvc.perform(post("/check/auth")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(given))
                        .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(REQUIRE_MAIL_DUPLICATE_CHECK.getMessage()))
                .andDo(print());
        
        verify(signupService, times(1)).sendEmailAuthCode(sessionId, given);
    }
    
    @DisplayName("인증 코드 메일 발송 - success")
    @Test
    void send_email_success() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        String sessionId = session.getId();
        
        EmailAuthCodeRequest given = EmailAuthCodeRequest.of(EMAIL);
        
        doNothing().when(signupService).sendEmailAuthCode(sessionId, given);
        
        // when-then
        mvc.perform(post("/check/auth")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(given))
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(SEND_EMAIL_AUTH_CODE.getMessage()))
                .andDo(print());
        
        verify(signupService, times(1)).sendEmailAuthCode(sessionId, given);
    }
}
