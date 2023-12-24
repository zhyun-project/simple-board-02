package kim.zhyun.serveruser.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kim.zhyun.serveruser.entity.SessionUser;
import kim.zhyun.serveruser.repository.SessionUserRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
@Component
public class ConnectionInterceptor implements HandlerInterceptor {
    private final SessionUserRedisRepository sessionUserRedisRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        
        log.debug("save session id: {}", sessionId);
        
        if (!sessionUserRedisRepository.existsById(sessionId)) {
            sessionUserRedisRepository.save(SessionUser.builder()
                    .sessionId(sessionId)
                    .build());
        }
        
        return true;
    }
    
}
