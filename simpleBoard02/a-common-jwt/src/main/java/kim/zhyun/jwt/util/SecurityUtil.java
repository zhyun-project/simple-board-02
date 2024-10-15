package kim.zhyun.jwt.util;

import kim.zhyun.jwt.domain.dto.JwtAuthentication;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static String getJwt() {
        return getJwtAuthentication().token();
    }

    public static long getUserId() {
        return getJwtUserInfoDto().getId();
    }

    public static JwtUserInfoDto getJwtUserInfoDto() {
        return getJwtAuthentication().jwtUserInfoDto();
    }

    public static JwtAuthentication getJwtAuthentication() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }

}
