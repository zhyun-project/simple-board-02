package kim.zhyun.serveruser.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
@Component
public class ConnectionInterceptor implements HandlerInterceptor {
    private final SessionUserService sessionUserService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String sessionId = request.getSession().getId();
        
        log.debug("save session id: {}", sessionId);
        
        if (!sessionUserService.existsById(sessionId)) {
            sessionUserService.save(SessionUser.builder()
                    .sessionId(sessionId)
                    .build());
        }
        
        // session user expire 시간 초기화
        sessionUserService.initSessionUserExpireTime(sessionId);
        
        return true;
    }
    
}
