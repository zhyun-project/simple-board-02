package kim.zhyun.jwt.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import kim.zhyun.jwt.common.constants.JwtConstants;
import kim.zhyun.jwt.domain.dto.JwtAuthentication;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.domain.service.JwtLogoutService;
import kim.zhyun.jwt.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static kim.zhyun.jwt.common.constants.JwtExceptionMessageConstants.JWT_EXPIRED;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends GenericFilterBean {
    private final JwtProvider provider;
    private final JwtLogoutService jwtLogoutService;

    private final List<String> jwtCheckApis = List.of(
            "/api/user/member",
            "/api/article/save",
            "/api/article/delete",
            "/api/article/update"
    );


    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String requestURI = httpServletRequest.getRequestURI();
        Optional<String> jwtHeaderContainer  = Optional.ofNullable(httpServletRequest.getHeader(JwtConstants.JWT_HEADER));

        boolean isJwtRequiredApi = jwtCheckApis.stream().anyMatch(requestURI::contains);

        if (isJwtRequiredApi && jwtHeaderContainer.isPresent()) {
            String jwt = jwtHeaderContainer.get().split(" ")[1];

            if (jwtLogoutService.isLogoutToken(jwt))
                throw new JwtException(JWT_EXPIRED);

            JwtAuthentication authentication = provider.authenticationFrom(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            JwtUserInfoDto userInfoDto = authentication.jwtUserInfoDto();

            log.info("Security Context에 {} {}({}) 인증 정보를 저장했습니다. uri: {}",
                    userInfoDto.getGrade(),
                    userInfoDto.getNickname(),
                    userInfoDto.getEmail(),
                    requestURI
            );
        }

        chain.doFilter(request, response);
    }
    
}
