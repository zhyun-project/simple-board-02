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

    public static JwtUserInfoDto toDto(Object principal) {
        return (JwtUserInfoDto) principal;

//        if (principal instanceof JwtUserInfoDto source) {
//            return source;
//        }
//
//        log.info("⚠️ == `principal` 객체가 `JwtUserInfoDto`가 아님");
//        return new JwtUserInfoDto();
    }

}
