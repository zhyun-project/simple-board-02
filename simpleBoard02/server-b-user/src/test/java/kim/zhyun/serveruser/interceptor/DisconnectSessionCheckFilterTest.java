package kim.zhyun.serveruser.interceptor;

import kim.zhyun.serveruser.config.SecurityConfig;
import kim.zhyun.serveruser.data.NicknameDto;
import kim.zhyun.serveruser.data.SignInRequest;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.service.MemberService;
import kim.zhyun.serveruser.service.NicknameReserveService;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Slf4j
@DisplayName("Disconnection SessionCheck Test - /sign-up, /check/* 이외의 end point 접근")
@Import(SecurityConfig.class)
@ExtendWith(RedisTestContainer.class)
@AutoConfigureMockMvc
@SpringBootTest
class DisconnectSessionCheckFilterTest {
    
    private final SessionUserService sessionUserService;
    private final NicknameReserveService nicknameReserveService;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Mock private MemberService memberService;
    
    private final MockMvc mvc;
    public DisconnectSessionCheckFilterTest(@Autowired MockMvc mvc,
                                            @Autowired SessionUserService sessionUserService,
                                            @Autowired NicknameReserveService nicknameReserveService,
                                            @Autowired RedisTemplate<String, String> redisTemplate) {
        this.mvc = mvc;
        this.sessionUserService = sessionUserService;
        this.nicknameReserveService = nicknameReserveService;
        this.redisTemplate = redisTemplate;
    }

    @DisplayName("/login post 접근")
    @Test
    void sign_in_post_test() throws Exception {
        run(post("/login"));
    }
    
    @DisplayName("/sign-out get 접근")
    @Test
    void sign_out_get_test() throws Exception {
        run(get("/sign-out"));
    }
    
    @DisplayName("/withdrawal delete 접근")
    @Test
    void withdrawal_delete_test() throws Exception {
        run(delete("/withdrawal"));
    }
    
    @DisplayName("/user get 접근")
    @Test
    void user_get_all() throws Exception {
        run(get("/user"));
    }
    
    @DisplayName("/user/{id} get 접근")
    @Test
    void user_get_by_id() throws Exception {
        run(get("/user/{id}", 1));
    }
    
    @DisplayName("/user/{id} put 접근")
    @Test
    void user_update_by_id() throws Exception {
        run(put("/user/{id}", 1));
    }
    
    @DisplayName("/user/{id}/role put 접근")
    @Test
    void user_update_by_id_and_role() throws Exception {
        run(put("/user/{id}/role", 1));
    }
    
    private void run(MockHttpServletRequestBuilder mockHttpServletRequestBuilder) throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        String sessionId = session.getId();
        
        var reservedNickname = "reservedNickname";
        var resultSavedSessionUserContainNickname = SessionUser.builder()
                .sessionId(sessionId)
                .nickname(reservedNickname).build();
        var nicknameReserved = NicknameDto.builder()
                .nickname(reservedNickname)
                .sessionId(sessionId).build();
        
        sessionUserService.save(resultSavedSessionUserContainNickname);
        nicknameReserveService.saveNickname(nicknameReserved);
        
        assertTrue(sessionUserService.existsById(sessionId));
        assertTrue(redisTemplate.hasKey(nicknameReserved.getNickname()));
        
        
        // when
        mvc.perform(mockHttpServletRequestBuilder
                        .contentType(APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(SignInRequest.of("test@test.test", "test")))
                        .session(session))
                .andDo(print());
        
        // then
        assertFalse(sessionUserService.existsById(sessionId));
        assertFalse(redisTemplate.hasKey(nicknameReserved.getNickname()));
    }
    
}
