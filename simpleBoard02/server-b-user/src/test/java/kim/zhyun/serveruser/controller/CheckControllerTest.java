package kim.zhyun.serveruser.controller;

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
import org.springframework.test.web.servlet.MockMvc;

import static kim.zhyun.serveruser.data.message.ResponseMessage.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    
    @MockBean
    private SignUpService signupService;
    
    private final MockMvc mvc;
    public CheckControllerTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
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
        when(signupService.availableNickname(anyString(), anyString())).thenReturn(true);
        
        mvc.perform(get("/check").param("nickname", NICKNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(true))
                .andExpect(jsonPath("message").value(SIGN_UP_AVAILABLE_NICKNAME.getMessage()))
                .andDo(print());
        
        verify(signupService, times(0)).availableEmail(anyString(), anyString());
        verify(signupService, times(1)).availableNickname(anyString(), anyString());
    }
    
    @DisplayName("닉네임 중복 확인 - 사용 불가")
    @Test
    void duplicate_check_nickname_using() throws Exception {
        when(signupService.availableNickname(anyString(), anyString())).thenReturn(false);
        
        mvc.perform(get("/check").param("nickname", NICKNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(false))
                .andExpect(jsonPath("message").value(SIGN_UP_UNAVAILABLE_NICKNAME.getMessage()))
                .andDo(print());
        
        verify(signupService, times(0)).availableEmail(anyString(), anyString());
        verify(signupService, times(1)).availableNickname(anyString(), anyString());
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
                .andExpect(jsonPath("$.result.[0].message").value("올바른 이메일 주소를 입력해주세요."))
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
                .andExpect(jsonPath("$.result.[0].message").value("올바른 이메일 주소를 입력해주세요."))
                .andDo(print());
        
        verify(signupService, times(0)).availableEmail(anyString(), anyString());
        verify(signupService, times(0)).availableNickname(anyString(), anyString());
    }
}