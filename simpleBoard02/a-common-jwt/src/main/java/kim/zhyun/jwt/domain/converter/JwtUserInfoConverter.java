package kim.zhyun.jwt.domain.converter;

import kim.zhyun.jwt.domain.dto.JwtAuthentication;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtUserInfoConverter {

    public JwtUserInfoDto toDto(JwtUserInfoEntity entity) {
        return JwtUserInfoDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .grade(entity.getGrade())
                .build();
    }

    public static JwtUserInfoDto toDto(Authentication authentication) {
        return ((JwtAuthentication) authentication).jwtUserInfoDto();
    }

}
