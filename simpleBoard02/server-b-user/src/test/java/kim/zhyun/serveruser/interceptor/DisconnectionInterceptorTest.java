package kim.zhyun.serveruser.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kim.zhyun.serveruser.entity.SessionUser;
import kim.zhyun.serveruser.service.NicknameStorageService;
import kim.zhyun.serveruser.service.SessionUserRedisService;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Slf4j
@DisplayName("Disconnection Interceptor Test - /sign-up, /check/* 이외의 end point 접근")
@SpringBootTest
class DisconnectionInterceptorTest {
    
    @Nested
    @DisplayName("Interceptor Method Call Test")
    @AutoConfigureMockMvc
    class DisconnectedInterceptorTest {
        
        @MockBean    private ConnectionInterceptor connectionInterceptor;
        @MockBean    private DisconnectionInterceptor disconnectionInterceptor;
        
        private final MockMvc mvc;
        public DisconnectedInterceptorTest(@Autowired MockMvc mvc) {
            this.mvc = mvc;
        }
        
        @DisplayName("/sign-in post 접근")
        @Test
        void sign_in_post_test() throws Exception {
            verifyRun(post("/sign-in"));
        }
        
        @DisplayName("/sign-out get 접근")
        @Test
        void sign_out_get_test() throws Exception {
            verifyRun(get("/sign-out"));
        }
        
        @DisplayName("/withdrawal delete 접근")
        @Test
        void withdrawal_delete_test() throws Exception {
            verifyRun(delete("/withdrawal"));
        }
        
        @DisplayName("/user get 접근")
        @Test
        void user_get_all() throws Exception {
            verifyRun(get("/user"));
        }
        
        @DisplayName("/user/{id} get 접근")
        @Test
        void user_get_by_id() throws Exception {
            verifyRun(get("/user/{id}", 1));
        }
        
        @DisplayName("/user/{id} put 접근")
        @Test
        void user_update_by_id() throws Exception {
            verifyRun(put("/user/{id}", 1));
        }
        
        @DisplayName("/user/{id}/role put 접근")
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
    
    @Nested
    @DisplayName("SessionService Call Test")
    @AutoConfigureMockMvc
    class DisconnectedSessionTest {
        
        @InjectMocks    private ConnectionInterceptor connectionInterceptor;
        @InjectMocks    private DisconnectionInterceptor disconnectionInterceptor;
        @Mock           private SessionUserRedisService sessionUserRedisService;
        @Mock           private NicknameStorageService nicknameStorageService;
        
        @DisplayName("모든 interceptor에서 SessionService 호출이 한군데에서만 발생하는지 검증")
        @Nested
        class SessionServiceCallTimeCheckInAllInterceptorMethod {
            
            @DisplayName("/sign-in post 접근")
            @Test
            void sign_in_post_test() throws Exception {
                run(post("/sign-in"));
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
                // when
                MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
                String sessionId = mockHttpServletRequest.getSession().getId();
                
                when(sessionUserRedisService.findById(sessionId)).thenReturn(Optional.empty());
                
                connectionInterceptor.preHandle(mockHttpServletRequest, new MockHttpServletResponse(), new Object());
                connectionInterceptor.postHandle(mockHttpServletRequest, new MockHttpServletResponse(), new Object(), null);
                connectionInterceptor.afterCompletion(mockHttpServletRequest, new MockHttpServletResponse(), new Object(), null);
                
                disconnectionInterceptor.preHandle(mockHttpServletRequest, new MockHttpServletResponse(), new Object());
                disconnectionInterceptor.postHandle(mockHttpServletRequest, new MockHttpServletResponse(), new Object(), null);
                disconnectionInterceptor.afterCompletion(mockHttpServletRequest, new MockHttpServletResponse(), new Object(), null);
                
                // then
                verify(sessionUserRedisService, times(1)).findById(sessionId);
            }
        }
        
        @DisplayName("의도한 interceptor method에서 SessionService 호출 횟수 검증")
        @Nested
        class SessionServiceCallTimeCheckInPreHandler {
            
            @DisplayName("nickname 중복 확인을 마친 session user가 저장됨")
            @Nested
            class SessionServiceCallTimeCheckCase1 {
                
                @DisplayName("sign-in post 접근")
                @Test
                void sign_in_post_test() throws Exception {
                    run(post("/sign-in"));
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
                    // when
                    MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
                    String sessionId = mockHttpServletRequest.getSession().getId();
                    
                    var reservedNickname = "reservedNickname";
                    var resultSavedSessionUserContainNickname   = Optional.of(SessionUser.builder()
                            .sessionId(sessionId)
                            .nickname(reservedNickname).build());
            
                    when(sessionUserRedisService.findById(sessionId)).thenReturn(resultSavedSessionUserContainNickname);
                    doNothing().when(nicknameStorageService).deleteNickname(reservedNickname);
                    doNothing().when(sessionUserRedisService).deleteById(sessionId);
                
                    disconnectionInterceptor.preHandle(mockHttpServletRequest, new MockHttpServletResponse(), new Object());
                    
                    // then
                    verify(sessionUserRedisService, times(1)).findById(sessionId);
                    verify(nicknameStorageService, times(1)).deleteNickname(reservedNickname);
                    verify(sessionUserRedisService, times(1)).deleteById(sessionId);
                }
            }
            
            @DisplayName("nickname 중복 확인을 하지 않은 session user가 저장됨")
            @Nested
            class SessionServiceCallTimeCheckCase2 {
                
                @DisplayName("/sign-in post 접근")
                @Test
                void sign_in_post_test() throws Exception {
                    run(post("/sign-in"));
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
                    // when
                    MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
                    String sessionId = mockHttpServletRequest.getSession().getId();
                    
                    var resultSavedSessionUser = Optional.of(SessionUser.builder()
                            .sessionId(sessionId).build());
                    
                    when(sessionUserRedisService.findById(sessionId)).thenReturn(resultSavedSessionUser);
                    doNothing().when(sessionUserRedisService).deleteById(sessionId);
                    
                    disconnectionInterceptor.preHandle(mockHttpServletRequest, new MockHttpServletResponse(), new Object());
                    
                    // then
                    verify(sessionUserRedisService, times(1)).findById(sessionId);
                    verify(nicknameStorageService,  times(0)).deleteNickname("");
                    verify(sessionUserRedisService, times(1)).deleteById(sessionId);
                }
            }
            
        }
    }
    
}
