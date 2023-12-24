package kim.zhyun.serveruser.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
class InterceptorTest {
    
    @MockBean    private ConnectionInterceptor connectionInterceptor;
    @MockBean    private DisconnectionInterceptor disconnectionInterceptor;

    private final MockMvc mvc;
    public InterceptorTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
    }
    
    @DisplayName("Connected Test - /sign-up, /check/* 접근")
    @Nested
    class ConnectedTest {
        @Test
        void sign_up_test() throws Exception {
            verifyRun(post("/sign-up"));
        }
        
        @Test
        void check_test() throws Exception {
            verifyRun(get("/check"));
        }
        
        @Test
        void check_auth_get_test() throws Exception {
            verifyRun(get("/check/auth"));
        }
        
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

    @DisplayName("Disconnected Test - /sign-up, /check/* 이외의 end point 접근")
    @Nested
    class DisconnectedTest {
        
        @Test
        void sign_in_post_test() throws Exception {
            verifyRun(post("/sign-in"));
        }
        
        @Test
        void sign_out_get_test() throws Exception {
            verifyRun(get("/sign-out"));
        }
        
        @Test
        void withdrawal_delete_test() throws Exception {
            verifyRun(delete("/withdrawal"));
        }
        
        @Test
        void user_get_all() throws Exception {
            verifyRun(get("/user"));
        }
        
        @Test
        void user_get_by_id() throws Exception {
            verifyRun(get("/user/{id}", 1));
        }
        
        @Test
        void user_update_by_id() throws Exception {
            verifyRun(put("/user/{id}", 1));
        }
        
        @Test
        void user_update_by_id_and_role() throws Exception {
            verifyRun(put("/user/{id}/role", 1));
        }
        
        private void verifyRun(MockHttpServletRequestBuilder mockHttpServletRequestBuilder) throws Exception {
            // when
            MockHttpSession session = new MockHttpSession();
            mvc.perform(mockHttpServletRequestBuilder.session(session))
                    .andDo(print());
            
            // then
            verify(connectionInterceptor, times(0)).preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any());
            verify(connectionInterceptor, times(0)).postHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any(), any());
            verify(connectionInterceptor, times(0)).afterCompletion(any(HttpServletRequest.class), any(HttpServletResponse.class), any(), any());
            
            verify(disconnectionInterceptor, times(1)).preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any());
            verify(disconnectionInterceptor, times(0)).postHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any(), any());
            verify(disconnectionInterceptor, times(0)).afterCompletion(any(HttpServletRequest.class), any(HttpServletResponse.class), any(), any());
        }
    }
    
}
