package kim.zhyun.serveruser.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kim.zhyun.serveruser.entity.SessionUser;
import kim.zhyun.serveruser.service.NicknameStorageService;
import kim.zhyun.serveruser.service.SessionUserRedisService;
import kim.zhyun.serveruser.service.impl.NicknameStorageServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class DisconnectionInterceptor implements HandlerInterceptor {
    private final SessionUserRedisService sessionUserRedisService;
    private final NicknameStorageService nicknameStorageService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        
        log.info("delete nickname cache - session id: {}", sessionId);

        // 1. session_id storage : nickname 값 조회
        Optional<SessionUser> optionalSessionUser = sessionUserRedisService.findById(sessionId);
        if (optionalSessionUser.isPresent()) {
            
            // 2. nickname storage : 1에서 조회한 nickname 삭제
            String nickname = optionalSessionUser.get().getNickname();
            if (nickname != null && !nickname.isBlank()) {
                nicknameStorageService.deleteNickname(nickname);
            }
            
            // 3. session_id storage : session_id 삭제
            sessionUserRedisService.deleteById(sessionId);
        }
        
        return true;
    }
    
}
