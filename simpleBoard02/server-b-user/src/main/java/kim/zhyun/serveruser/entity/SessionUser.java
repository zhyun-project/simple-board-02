package kim.zhyun.serveruser.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
@RedisHash("SESSION_ID")
public class SessionUser {
    
    @Id
    private String sessionId;
    
    private String email;
    private String nickname;
    
}
