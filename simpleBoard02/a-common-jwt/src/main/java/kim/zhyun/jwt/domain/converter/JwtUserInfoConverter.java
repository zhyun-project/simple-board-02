package kim.zhyun.jwt.domain.converter;

import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import org.springframework.stereotype.Component;

@Component
public class JwtUserInfoConverter {

    public JwtUserInfoDto toDto(JwtUserInfoEntity entity) {
        return JwtUserInfoDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .build();
    }

}
