package kim.zhyun.serverarticle.util;

import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.jwt.data.JwtUserInfo;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.TestSecurityContextHolder;

import java.util.Set;

@Disabled("테스트 유틸")
public class TestSecurityUser {
    
    @DisplayName("Authentication 객체 설정")
    public static void setAuthentication(JwtUserInfo user) {
        JwtUserDto jwtUserDto = JwtUserDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .id(user.getId()).build();
        
        SecurityContext securityContext = TestSecurityContextHolder.getContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                jwtUserDto, "",
                Set.of(new SimpleGrantedAuthority(user.getGrade()))));
        TestSecurityContextHolder.setContext(securityContext);
    }
}
