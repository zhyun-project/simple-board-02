package kim.zhyun.serveruser.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Objects;

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
    
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SessionUser that = (SessionUser) obj;
        
        if (!Objects.equals(sessionId, that.sessionId)) return false;
        if (!Objects.equals(email, that.email)) return false;
        return Objects.equals(nickname, that.nickname);
    }
    
}
