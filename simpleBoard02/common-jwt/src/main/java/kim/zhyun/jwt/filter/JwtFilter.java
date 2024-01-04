package kim.zhyun.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import kim.zhyun.jwt.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import static kim.zhyun.jwt.data.JwtConstants.JWT_HEADER;
import static kim.zhyun.jwt.data.JwtConstants.JWT_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends GenericFilterBean {
    private final JwtProvider provider;
    
    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String jwt = getToken(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();
        
        if (StringUtils.hasText(jwt) && provider.validateToken(jwt)) {
            Authentication authentication = provider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Security Context에 {} 인증 정보를 저장했습니다. uri: {}",
                    authentication.getName(),
                    requestURI);
        } else {
            log.info("유효한 JWT 토큰이 없습니다. uri: {}", requestURI);
        }
        
        chain.doFilter(request, response);
    }
    
    private String getToken(HttpServletRequest request) {
        String jwt = request.getHeader(JWT_HEADER);
        
        if (StringUtils.hasText(jwt) && jwt.startsWith(JWT_PREFIX)) {
            return jwt.substring(JWT_PREFIX.length());
        }
        
        return null;
    }
    
}
