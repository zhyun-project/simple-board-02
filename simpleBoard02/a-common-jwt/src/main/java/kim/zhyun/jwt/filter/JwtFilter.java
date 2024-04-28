package kim.zhyun.jwt.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import kim.zhyun.jwt.constants.JwtConstants;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.jwt.service.JwtLogoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import static kim.zhyun.jwt.constants.JwtConstants.JWT_PREFIX;
import static kim.zhyun.jwt.constants.JwtExceptionMessageConstants.JWT_EXPIRED;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends GenericFilterBean {
    private final JwtProvider provider;
    private final JwtLogoutService jwtLogoutService;
    
    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String requestURI = httpServletRequest.getRequestURI();
        String jwtHeader  = httpServletRequest.getHeader(JwtConstants.JWT_HEADER);
        
        if (Strings.isNotBlank(jwtHeader) && jwtHeader.length() > JWT_PREFIX.length()) {
            String jwt = jwtHeader.substring(JWT_PREFIX.length());
            
            if (jwtLogoutService.isLogoutToken(jwt, provider.emailFrom(jwt)))
                throw new JwtException(JWT_EXPIRED);
            
            Authentication authentication = provider.authenticationFrom(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.info("Security Context에 {} {}({}) 인증 정보를 저장했습니다. uri: {}",
                    provider.gradeFrom(authentication),
                    provider.nicknameFrom(authentication),
                    provider.emailFrom(authentication),
                    requestURI
            );
        }
        
        chain.doFilter(request, response);
    }
    
}
