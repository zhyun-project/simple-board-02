package kim.zhyun.serveruser.domain.signup.controller;

import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.jwt.filter.JwtFilter;
import kim.zhyun.serveruser.common.message.ExceptionMessage;
import kim.zhyun.serveruser.config.TestSecurityConfig;
import kim.zhyun.serveruser.domain.signup.business.SignUpBusiness;
import kim.zhyun.serveruser.domain.signup.controller.model.SignupRequest;
import kim.zhyun.serveruser.filter.AuthenticationFilter;
import kim.zhyun.serveruser.filter.SessionCheckFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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

import static kim.zhyun.serveruser.common.message.ResponseMessage.RESPONSE_SUCCESS_FORMAT_SIGN_UP;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@WebMvcTest(
        controllers = SignApiController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        AuthenticationFilter.class,
                        JwtFilter.class,
                        SessionCheckFilter.class
                }
        )
)
class SignApiControllerTest {
    
    @MockBean SignUpBusiness signUpBusiness;
    
    @Autowired MockMvc mvc;
    ObjectMapper objectMapper = new ObjectMapper();
    
    
    @DisplayName("회원가입 - 성공")
    @Test
    void signUp_success() throws Exception {
        // given
        SignupRequest signupRequest = SignupRequest.of(
                "new@email.mail", "닉네임", "password"
        );
        
        MockHttpSession session = new MockHttpSession();
        
        String responseMessage = RESPONSE_SUCCESS_FORMAT_SIGN_UP;
        
        given(signUpBusiness.saveMember(session.getId(), signupRequest)).willReturn(responseMessage);
        
        
        // when - then
        mvc.perform(
                post("/sign-up")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(signupRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    
    @DisplayName("회원가입 - 실패: email 혹은 nickname 중복확인 안함")
    @ParameterizedTest
    @ValueSource(strings = {
            ExceptionMessage.EXCEPTION_REQUIRE_MAIL_DUPLICATE_CHECK,
            ExceptionMessage.EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK
    })
    void signUp_fail(String responseMessage) throws Exception {
        // given
        SignupRequest signupRequest = SignupRequest.of(
                "new@email.mail", "닉네임", "password"
        );
        
        MockHttpSession session = new MockHttpSession();
        
        given(signUpBusiness.saveMember(session.getId(), signupRequest)).willThrow(new ApiException(responseMessage));
        
        
        // when - then
        mvc.perform(
                        post("/sign-up")
                                .session(session)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(signupRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
}
