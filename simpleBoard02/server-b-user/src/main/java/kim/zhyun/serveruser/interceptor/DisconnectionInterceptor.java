package kim.zhyun.serveruser.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
@Component
public class DisconnectionInterceptor implements HandlerInterceptor {
    private final SessionUserService sessionUserService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String sessionId = request.getSession().getId();
        
        log.debug("delete nickname cache - session id: {}", sessionId);

        // session_id storage - session_id 삭제, nickname storage - nickname 예약 삭제
        if (sessionUserService.existsById(sessionId)) {
            sessionUserService.deleteById(sessionId);
        }
        
        return true;
    }
    
}
