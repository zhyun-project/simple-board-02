package kim.zhyun.serveruser.filter;

import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.jwt.filter.JwtFilter;
import kim.zhyun.serveruser.config.TestSecurityConfig;
import kim.zhyun.serveruser.domain.member.service.SessionUserService;
import kim.zhyun.serveruser.domain.signup.repository.SessionUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(
        value = SessionCheckFilter.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        JwtFilter.class,
                        AuthenticationFilter.class
                }
        )
)
class SessionCheckFilterTest {
    
    @MockBean SessionUserService sessionUserService;
    
    @Autowired MockMvc mvc;
    
    
    
    @DisplayName("회원가입, 유저 정보 수정, 닉네임 중복확인, 이메일 중복확인 api 접근")
    @ParameterizedTest(name = "{0}, exist session id = {2}")
    @MethodSource
    @WithMockUser(roles = RoleType.TYPE_ADMIN)
    void doFilterInternal(String apiName, MockHttpServletRequestBuilder method, boolean existSessionId) throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        
        given(sessionUserService.existsById(session.getId())).willReturn(existSessionId);
        if (!existSessionId) {
            willDoNothing().given(sessionUserService).save(any(SessionUser.class));
        }
        willDoNothing().given(sessionUserService).initSessionUserExpireTime(eq(session.getId()));
        
        
        // when
        mvc.perform(method.session(session))
                .andDo(MockMvcResultHandlers.print());
        
        
        // then
        then(sessionUserService).should(times(1))
                .existsById(eq(session.getId()));
        then(sessionUserService).should(times(existSessionId ? 0 : 1))
                .save(any(SessionUser.class));
        then(sessionUserService).should(times(1))
                .initSessionUserExpireTime(eq(session.getId()));
        
        then(sessionUserService).should(times(0))
                .deleteById(eq(session.getId()));
        
    }
    static Stream<Arguments> doFilterInternal() {
        return Stream.of(
                Arguments.of("get /check", get("/check"), true),
                Arguments.of("get /check", get("/check"), false),
                
                Arguments.of("post /check/auth", post("/check/auth"), true),
                Arguments.of("post /check/auth", post("/check/auth"), false),
                
                Arguments.of("get /check/auth", get("/check/auth"), true),
                Arguments.of("get /check/auth", get("/check/auth"), false),
                
                Arguments.of("post /sign-up", post("/sign-up"), true),
                Arguments.of("post /sign-up", post("/sign-up"), false),
                
                Arguments.of("put /{id}", put("/{id}", 9237L), true),
                Arguments.of("put /{id}", put("/{id}", 9237L), false)
        );
    }
    
    
    @DisplayName("disconnectProcess()")
    @ParameterizedTest(name = "{0}")
    @MethodSource
    @WithMockUser(roles = RoleType.TYPE_ADMIN)
    void disconnectProcess_others(String apiName, MockHttpServletRequestBuilder method) throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        
        given(sessionUserService.existsById(session.getId())).willReturn(true);
        willDoNothing().given(sessionUserService).deleteById(eq(session.getId()));
        
        
        // when
        mvc.perform(method.session(session))
                .andDo(MockMvcResultHandlers.print());
        
        
        // then
        then(sessionUserService).should(times(1))
                .existsById(session.getId());
        then(sessionUserService).should(times(1))
                .deleteById(eq(session.getId()));
        
        then(sessionUserService).should(times(0))
                .save(any(SessionUser.class));
        then(sessionUserService).should(times(0))
                .initSessionUserExpireTime(eq(session.getId()));
    }
    static Stream<Arguments> disconnectProcess_others() {
        return Stream.of(
                Arguments.of("get /all", get("/all")),
                Arguments.of("get /all", get("/all")),
                
                Arguments.of("get /{id}", get("/{id}", 123L)),
                Arguments.of("get /{id}", get("/{id}", 123L)),
                
                Arguments.of("put /role", put("/role")),
                Arguments.of("put /role", put("/role")),
                
                Arguments.of("post /logout", post("/logout")),
                Arguments.of("post /logout", post("/logout")),
                
                Arguments.of("post /withdrawal", post("/withdrawal"), 9237L),
                Arguments.of("post /withdrawal", post("/withdrawal"), 9237L)
        );
    }
    
}

