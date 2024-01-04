package kim.zhyun.serveruser.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class SessionCheckFilter extends OncePerRequestFilter {
    private final SessionUserService sessionUserService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String sessionId = request.getSession().getId();
        
        // 회원가입, 닉네임, 이메일 중복확인 end point 접근
        if (requestURI.contains("sign-up") || requestURI.contains("check")) {
            connectProcess(sessionId);
        }
        // 이외의 end point 접근
        else {
            disconnectProcess(sessionId);
        }
        
        filterChain.doFilter(request, response);
    }
    
    
    private void disconnectProcess(String sessionId) {
        log.debug("delete nickname cache - session id: {}", sessionId);
        
        // session_id storage - session_id 삭제, nickname storage - nickname 예약 삭제
        if (sessionUserService.existsById(sessionId)) {
            sessionUserService.deleteById(sessionId);
        }
    }
    
    private void connectProcess(String sessionId) {
        log.debug("save session id: {}", sessionId);
        
        if (!sessionUserService.existsById(sessionId)) {
            sessionUserService.save(SessionUser.builder()
                    .sessionId(sessionId)
                    .build());
        }
        
        // session user expire 시간 초기화
        sessionUserService.initSessionUserExpireTime(sessionId);
    }
    
}
