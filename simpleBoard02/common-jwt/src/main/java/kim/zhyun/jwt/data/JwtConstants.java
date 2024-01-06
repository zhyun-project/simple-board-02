package kim.zhyun.jwt.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

@Component
public class JwtConstants {
    
    public static final String JWT_HEADER = "X-TOKEN";
    public static final String JWT_PREFIX = "Bearer ";
    public static final String JWT_CLAIM_KEY_USER_GRADE = "grade";
    public static final String JWT_CLAIM_KEY_USER_ID = "id";
    public static final String JWT_CLAIM_KEY_USER_NICKNAME = "nickname";
    public static final String JWT_CLAIM_GRADE_SEPARATOR = ",";

    
    @Value("${token.expiration-time}")  public Long expiredTime;
    @Value("${token.secret}")           public String secretKey;
    public TimeUnit expiredTimeUnit;
    
    
    public JwtConstants(@Value("${token.expiration-time-unit}")  String expiredTimeUnitString) {
        expiredTimeUnit = expiredTimeUnitString.equalsIgnoreCase("d") ? DAYS
                : expiredTimeUnitString.equalsIgnoreCase("h") ? HOURS
                : expiredTimeUnitString.equalsIgnoreCase("m") ? MINUTES
                : SECONDS;
    }
    
}
