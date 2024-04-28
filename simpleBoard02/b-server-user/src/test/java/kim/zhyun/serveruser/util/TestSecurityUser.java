package kim.zhyun.serveruser.util;

import kim.zhyun.jwt.dto.JwtUserInfoDto;
import kim.zhyun.serveruser.domain.member.repository.UserEntity;
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
    public static void setAuthentication(UserEntity userEntity) {
        JwtUserInfoDto jwtUserInfoDto = JwtUserInfoDto.builder()
                .email(userEntity.getEmail())
                .nickname(userEntity.getNickname())
                .id(userEntity.getId()).build();
        
        SecurityContext securityContext = TestSecurityContextHolder.getContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                jwtUserInfoDto, "",
                Set.of(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().getGrade()))));
        TestSecurityContextHolder.setContext(securityContext);
    }
}
