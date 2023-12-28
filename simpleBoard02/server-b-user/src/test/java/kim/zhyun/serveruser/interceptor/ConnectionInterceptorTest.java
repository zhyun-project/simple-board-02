package kim.zhyun.serveruser.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Slf4j
@DisplayName("Connection Interceptor Test - /sign-up, /check/* 접근")
@SpringBootTest
class ConnectionInterceptorTest {
    
    @Nested
    @DisplayName("Interceptor Method Call Test")
    @AutoConfigureMockMvc
    class ConnectedInterceptorTest {
        
        @MockBean    private ConnectionInterceptor connectionInterceptor;
        @MockBean    private DisconnectionInterceptor disconnectionInterceptor;
        
        private final MockMvc mvc;
        public ConnectedInterceptorTest(@Autowired MockMvc mvc) {
            this.mvc = mvc;
        }
        
        @DisplayName("/sign-up post 접근")
        @Test
        void sign_up_test() throws Exception {
            verifyRun(post("/sign-up"));
        }
        
        @DisplayName("/check get 접근")
        @Test
        void check_test() throws Exception {
            verifyRun(get("/check"));
        }
        
        @DisplayName("/check/auth get 접근")
        @Test
        void check_auth_get_test() throws Exception {
            verifyRun(get("/check/auth"));
        }
        
        @DisplayName("/check/auth post 접근")
        @Test
        void check_auth_post_test() throws Exception {
            verifyRun(post("/check/auth"));
        }
        
        private void verifyRun(MockHttpServletRequestBuilder mockHttpServletRequestBuilder) throws Exception {
            // when
            MockHttpSession session = new MockHttpSession();
            mvc.perform(mockHttpServletRequestBuilder.session(session))
                    .andDo(print());
            
            // then
            verify(connectionInterceptor, times(1)).preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any());
            verify(connectionInterceptor, times(0)).postHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any(), any());
            verify(connectionInterceptor, times(0)).afterCompletion(any(HttpServletRequest.class), any(HttpServletResponse.class), any(), any());
            
            verify(disconnectionInterceptor, times(0)).preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any());
            verify(disconnectionInterceptor, times(0)).postHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any(), any());
            verify(disconnectionInterceptor, times(0)).afterCompletion(any(HttpServletRequest.class), any(HttpServletResponse.class), any(), any());
        }
    }
    
    @Nested
    @DisplayName("SessionService Call Test")
    class ConnectedSessionTest {
        
        @InjectMocks    private ConnectionInterceptor connectionInterceptor;
        @InjectMocks    private DisconnectionInterceptor disconnectionInterceptor;
        @Mock           private SessionUserService sessionUserService;
        
        @DisplayName("/sign-up post 접근 - 2번째 파라미터 : session id가 redis에 저장돼있는지 유무")
        @Test
        void sign_up_test() throws Exception {
            run(post("/sign-up"), false);
            run(post("/sign-up"), true);
        }
        
        @DisplayName("/check get 접근 - 2번째 파라미터 : session id가 redis에 저장돼있는지 유무")
        @Test
        void check_test() throws Exception {
            run(get("/check"), false);
            run(get("/check"), true);
        }
        
        @DisplayName("/check/auth get 접근 - 2번째 파라미터 : session id가 redis에 저장돼있는지 유무")
        @Test
        void check_auth_get_test() throws Exception {
            run(get("/check/auth"), false);
            run(get("/check/auth"), true);
        }
        
        @DisplayName("/check/auth post 접근 - 2번째 파라미터 : session id가 redis에 저장돼있는지 유무")
        @Test
        void check_auth_post_test() throws Exception {
            run(post("/check/auth"), false);
            run(post("/check/auth"), true);
        }
        
        private void run(MockHttpServletRequestBuilder mockHttpServletRequestBuilder, boolean sessionIdExistInRedis) throws Exception {
            // when
            MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
            String sessionId = mockHttpServletRequest.getSession().getId();
            
            when(sessionUserService.existsById(sessionId)).thenReturn(sessionIdExistInRedis);
            
            connectionInterceptor.preHandle(mockHttpServletRequest, new MockHttpServletResponse(), new Object());
            connectionInterceptor.postHandle(mockHttpServletRequest, new MockHttpServletResponse(), new Object(), null);
            connectionInterceptor.afterCompletion(mockHttpServletRequest, new MockHttpServletResponse(), new Object(), null);
            
            // then
            SessionUser source = SessionUser.builder().sessionId(sessionId).build();
            
            verify(sessionUserService, times(1)).existsById(sessionId);
            verify(sessionUserService, times(sessionIdExistInRedis ? 0 : 1)).save(source);
        }
    }
    
}
