package kim.zhyun.jwt.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static kim.zhyun.jwt.util.TimeUnitUtil.timeUnitFrom;

@Component
public class JwtConstants {
    
    public static final String JWT_HEADER = "X-TOKEN";
    public static final String JWT_PREFIX = "Bearer ";
    public static final String JWT_CLAIM_KEY_USER_ID = "id";
    public static final String JWT_CLAIM_GRADE_SEPARATOR = ",";
    public static final String JWT_USER_INFO_KEY = "USER_INFO";

    
    @Value("${token.expiration-time}")  public Long expiredTime;
    @Value("${token.secret}")           public String secretKey;
    public TimeUnit expiredTimeUnit;
    
    
    public JwtConstants(@Value("${token.expiration-time-unit}")  String expiredTimeUnitString) {
        expiredTimeUnit = TimeUnit.of(timeUnitFrom(expiredTimeUnitString));
    }
    
}
