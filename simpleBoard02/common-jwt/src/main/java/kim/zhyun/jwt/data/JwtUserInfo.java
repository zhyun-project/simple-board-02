package kim.zhyun.jwt.data;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
@RedisHash("USER_INFO")
public class JwtUserInfo {
    
    @Id
    private Long id;
    private String email;
    private String nickname;
    private String grade;
    
}
