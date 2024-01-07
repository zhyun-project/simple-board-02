package kim.zhyun.serveruser.util;

import kim.zhyun.jwt.data.JwtUserDto;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Disabled("테스트 클래스 아님")
public class TestSecurityUser {
    
    @DisplayName("Authentication 객체 설정")
    public static void setAuthentication(String username, String nickname) {
        JwtUserDto jwtUserDto = JwtUserDto.builder()
                .email(username)
                .nickname(nickname)
                .id(1L).build();
        
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(jwtUserDto, "", securityContext.getAuthentication().getAuthorities()));
        SecurityContextHolder.setContext(securityContext);
    }
}
