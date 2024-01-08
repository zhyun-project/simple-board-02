package kim.zhyun.serveruser.util;

import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.serveruser.data.entity.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.TestSecurityContextHolder;

import java.util.Set;

@Disabled("테스트 유틸")
public class TestSecurityUser {
    
    @DisplayName("Authentication 객체 설정")
    public static void setAuthentication(User user) {
        JwtUserDto jwtUserDto = JwtUserDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .id(user.getId()).build();
        
        SecurityContext securityContext = TestSecurityContextHolder.getContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                jwtUserDto, "",
                Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getGrade()))));
        TestSecurityContextHolder.setContext(securityContext);
    }
}
