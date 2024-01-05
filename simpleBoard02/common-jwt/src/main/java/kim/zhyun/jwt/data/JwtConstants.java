package kim.zhyun.jwt.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConstants {
    
    public static final String JWT_HEADER = "X-TOKEN";
    public static final String JWT_PREFIX = "Bearer ";
    public static final String JWT_CLAIM_KEY_GRADE = "grade";
    public static final String JWT_CLAIM_GRADE_SEPARATOR = ",";
    
    @Value("${token.expiration-time}")  public Long expiredTime;
    @Value("${token.secret}")           public String secretKey;
    
}
