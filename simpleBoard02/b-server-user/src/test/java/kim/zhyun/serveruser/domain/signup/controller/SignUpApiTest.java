package kim.zhyun.serveruser.domain.signup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kim.zhyun.serveruser.common.message.ResponseMessage;
import kim.zhyun.serveruser.domain.signup.controller.model.EmailAuthCodeRequest;
import kim.zhyun.serveruser.domain.signup.controller.model.SignupRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@Disabled("전체 테스트 실행시 무시되도록 설정 켜야 됨")
@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SignUpApiTest {
    
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    
    
    @Order(1)
    @DisplayName("이메일 중복 확인")
    @ParameterizedTest
    @ValueSource(strings = {
            "gimwlgus@gmail.com",
            "gimwlgus@daum.net"
    })
    void duplicateCheck_email_success(String email) throws Exception {
        MockHttpSession session = new MockHttpSession();
        
        mvc.perform(
                        MockMvcRequestBuilders.get("/check")
                                .param("email", email)
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용 가능한 이메일입니다. 이메일 인증을 진행해주세요."))
                .andDo(print());
    }
    
    
    @Order(2)
    @DisplayName("닉네임 중복 확인 성공")
    @ParameterizedTest
    @ValueSource(strings = {
            "닉네임 🦆",
            "닉네임 😌"
    })
    void duplicateCheck_nickname_success(String nickname) throws Exception {
        MockHttpSession session = new MockHttpSession();
        
        mvc.perform(
                        MockMvcRequestBuilders.get("/check")
                                .param("nickname", nickname)
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("사용 가능한 닉네임입니다."))
                .andDo(print());
    }
    
    
    @Order(3)
    @DisplayName("이메일로 인증코드 전송")
    @ParameterizedTest
    @ValueSource(strings = {
            "gimwlgus@gmail.com",
            "gimwlgus@daum.net"
    })
    void sendEmail(String email) throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        
        EmailAuthCodeRequest request = EmailAuthCodeRequest.of(email);
        
        
        // when - then
        mvc.perform(
                        MockMvcRequestBuilders.post("/check/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request))
                                .session(session)
                )
                .andDo(print());
    }
    
    @Order(4)
    @DisplayName("메일 인증코드 검증")
    @ParameterizedTest
    @ValueSource(strings = {
            "de79bd",
            "a1c3db",
    })
    void authEmailCode(String requestCode) throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        
        // when - then
        mvc.perform(
                        MockMvcRequestBuilders.get("/check/auth")
                                .param("code", requestCode)
                                .session(session)
                )
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("인증되었습니다."))
                .andDo(print());
    }
    
    
    @DisplayName("회원가입")
    @ParameterizedTest
    @MethodSource
    void signUp(SignupRequest signupRequest) throws Exception {
        
        // when - then
        String responseMessage = ResponseMessage.RESPONSE_SUCCESS_FORMAT_SIGN_UP.formatted(signupRequest.getNickname());
        
        mvc.perform(
                        post("/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(signupRequest))
                )
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
        
    }
    static Stream<SignupRequest> signUp() {
        return Stream.of(
                SignupRequest.of(
                        "gimwlgus@gmail.com", "닉네임 🦆", "password"
                ),
                SignupRequest.of(
                        "gimwlgus@daum.net", "닉네임 😌", "password"
                )
        );
    }
    
}
