package kim.zhyun.jwt.common.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConstants {
    
    public static final String JWT_HEADER = "X-TOKEN";
    public static final String JWT_PREFIX = "Bearer";
    public static final String JWT_CLAIM_KEY_USER_ID = "id";
    public static final String JWT_CLAIM_GRADE_SEPARATOR = ",";
    public static final String JWT_USER_INFO_KEY = "USER_INFO";
    
    @Value("${token.secret}") public String secretKey;
    
}
