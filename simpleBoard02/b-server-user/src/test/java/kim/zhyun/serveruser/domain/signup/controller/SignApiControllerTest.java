package kim.zhyun.serveruser.domain.signup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kim.zhyun.serveruser.container.RedisTestContainer;
import kim.zhyun.serveruser.domain.signup.controller.model.SignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Order(0)
@ExtendWith(RedisTestContainer.class)
@AutoConfigureMockMvc
@SpringBootTest
class SignApiControllerTest {
    
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    
    
    
    @DisplayName("회원가입")
    @Test
    void signUp() throws Exception {
        // given
        SignupRequest signupRequest = SignupRequest.of(
                "new@email.mail", "닉네임", "password"
        );
        
        // when
        MvcResult mvcResult = mvc.perform(
                        post("/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(signupRequest))
                )
                .andDo(print())
                .andReturn();
        
        // then
        SignupRequest readBody = objectMapper.readValue(
                mvcResult.getRequest().getContentAsString(),
                SignupRequest.class
        );
        assertEquals(readBody, signupRequest);
    }
    
}