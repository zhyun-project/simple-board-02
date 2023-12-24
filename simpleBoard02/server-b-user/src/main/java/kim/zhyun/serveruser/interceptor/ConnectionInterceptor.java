package kim.zhyun.serveruser.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kim.zhyun.serveruser.entity.SessionUser;
import kim.zhyun.serveruser.service.SessionUserRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
@Component
public class ConnectionInterceptor implements HandlerInterceptor {
    private final SessionUserRedisService sessionUserRedisService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        
        log.info("save session id: {}", sessionId);
        
        if (!sessionUserRedisService.existsById(sessionId)) {
            sessionUserRedisService.save(SessionUser.builder()
                    .sessionId(sessionId)
                    .build());
        }
        
        return true;
    }
    
}
