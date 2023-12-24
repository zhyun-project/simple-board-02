package kim.zhyun.serveruser.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kim.zhyun.serveruser.entity.SessionUser;
import kim.zhyun.serveruser.repository.NicknameStorage;
import kim.zhyun.serveruser.repository.SessionUserRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class DisconnectionInterceptor implements HandlerInterceptor {
    private final SessionUserRedisRepository sessionUserRedisRepository;
    private final NicknameStorage nicknameStorage;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        
        log.debug("delete nickname cache - session id: {}", sessionId);

        // 1. session_id storage : nickname 값 조회
        Optional<SessionUser> optionalSessionUser = sessionUserRedisRepository.findById(sessionId);
        if (optionalSessionUser.isPresent()) {
            
            // 2. nickname storage : 1에서 조회한 nickname 삭제
            String nickname = optionalSessionUser.get().getNickname();
            if (nickname != null || !nickname.isBlank()) {
                nicknameStorage.deleteNickname(nickname);
            }
            
            // 3. session_id storage : session_id 삭제
            sessionUserRedisRepository.deleteById(sessionId);
        }
        
        return true;
    }
    
}
