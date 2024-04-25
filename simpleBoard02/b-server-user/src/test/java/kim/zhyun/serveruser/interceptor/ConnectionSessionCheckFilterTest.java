package kim.zhyun.serveruser.interceptor;

import kim.zhyun.serveruser.config.SecurityConfig;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.service.MemberService;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Slf4j
@DisplayName("Connection SessionFilter Test - /sign-up, /check/* 접근")
@Import(SecurityConfig.class)
@ExtendWith(RedisTestContainer.class)
@AutoConfigureMockMvc
@SpringBootTest
class ConnectionSessionCheckFilterTest {
    
    private final SessionUserService sessionUserService;
    
    @Mock private MemberService memberService;
    
    private final String context = "/api/user/";
    
    private final MockMvc mvc;
    public ConnectionSessionCheckFilterTest(@Autowired MockMvc mvc,
                                            @Autowired SessionUserService sessionUserService) {
        this.mvc = mvc;
        this.sessionUserService = sessionUserService;
    }
    
    @DisplayName("/sign-up post 접근")
    @Test
    void sign_up_test() throws Exception {
        verifyRun(post(context + "/sign-up"));
    }
    
    @DisplayName("/check get 접근")
    @Test
    void check_test() throws Exception {
        verifyRun(get(context + "/check"));
    }
    
    @DisplayName("/check/auth get 접근")
    @Test
    void check_auth_get_test() throws Exception {
        verifyRun(get(context + "/check/auth"));
    }
    
    @DisplayName("/check/auth post 접근")
    @Test
    void check_auth_post_test() throws Exception {
        verifyRun(post(context + "/check/auth"));
    }
    
    @DisplayName("/{id} put 접근")
    @Test
    void user_update_by_id() throws Exception {
        verifyRun(put(context + "/{id}", 1));
    }
    
    private void verifyRun(MockHttpServletRequestBuilder mockHttpServletRequestBuilder) throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        String sessionId = session.getId();
        
        var resultSavedSessionUserContainNickname = SessionUser.builder()
                .sessionId(sessionId).build();
        
        assertFalse(sessionUserService.existsById(sessionId));
        
        
        // when
        mvc.perform(mockHttpServletRequestBuilder.session(session))
                .andDo(print());
        
        
        // then
        assertTrue(sessionUserService.existsById(sessionId));
    }

}
