package kim.zhyun.serveruser.domain.signup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kim.zhyun.serveruser.container.RedisTestContainer;
import kim.zhyun.serveruser.domain.signup.controller.model.EmailAuthCodeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(0)
@ExtendWith(RedisTestContainer.class)
@AutoConfigureMockMvc
@SpringBootTest
class CheckApiControllerTest {
    
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    
    
    
    @DisplayName("중복 확인 실패 - 이메일 = null, 닉네임 = null")
    @Test
    void duplicateCheck_fail() throws Exception {
        MockHttpSession session = new MockHttpSession();
        
        mvc.perform(
                MockMvcRequestBuilders.get("/check")
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("값을 입력해주세요."))
                .andDo(print());
        
    }
    
    @DisplayName("이메일 중복 확인 성공")
    @ParameterizedTest
    @ValueSource(strings = {
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
            "1email@email.mail.ail",
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
        MockHttpSession session = new MockHttpSession();
        
        mvc.perform(
                MockMvcRequestBuilders.get("/check")
                        .param("email", email)
                        .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 값이 올바르지 않습니다."))
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
        MockHttpSession session = new MockHttpSession();
        
        mvc.perform(
                MockMvcRequestBuilders.get("/check")
                        .param("nickname", nickname)
                        .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("입력 값이 올바르지 않습니다."))
                .andDo(print());
    }
    
    
    @DisplayName("이메일로 인증코드 전송")
    @Test
    void sendEmail() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        
        EmailAuthCodeRequest request = EmailAuthCodeRequest.of("new@email.mail");
        
        
        // when
        MvcResult mvcResult = mvc.perform(
                        MockMvcRequestBuilders.post("/check/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request))
                                .session(session)
                )
                .andDo(print())
                .andReturn();
        
        
        // then
        EmailAuthCodeRequest emailAuthCodeRequest = objectMapper.readValue(
                mvcResult.getRequest().getContentAsString(),
                EmailAuthCodeRequest.class
        );
        
        assertEquals(emailAuthCodeRequest, request);
    }
    
    @DisplayName("메일 인증코드 검증")
    @Test
    void authEmailCode() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        
        String requestCode = "auth-code";
        
        
        // when
        MvcResult mvcResult = mvc.perform(
                        MockMvcRequestBuilders.get("/check/auth")
                                .param("code", requestCode)
                                .session(session)
                )
                .andDo(print())
                .andReturn();
        
        
        // then
        String readCode = mvcResult.getRequest().getParameterValues("code")[0];
        
        assertEquals(readCode, requestCode);
    }
    
}