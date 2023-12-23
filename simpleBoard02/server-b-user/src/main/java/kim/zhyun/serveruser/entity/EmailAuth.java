package kim.zhyun.serveruser.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
@RedisHash("EMAIL")
public class EmailAuth {
    
    @Id
    private String email;
    
    @Builder.Default
    private boolean isVerification = false;
    
    private String code;
    private LocalDateTime expiredAt;
    
    
    public static <T> boolean setKeyExpire(RedisTemplate<String, T> template, String key, Long minutes) {
        String emailKey = "EMAIL:" + key;
        return template.expire(emailKey, minutes, TimeUnit.MINUTES);
    }
    
    public static <T> long getKeyExpire(RedisTemplate<String, T> template, String key) {
        String emailKey = "EMAIL:" + key;
        return template.getExpire(emailKey, TimeUnit.MINUTES);
    }
    
}
