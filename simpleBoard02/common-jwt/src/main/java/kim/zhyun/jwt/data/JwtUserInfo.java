package kim.zhyun.jwt.data;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import static kim.zhyun.jwt.data.JwtConstants.JWT_USER_INFO_KEY;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
@RedisHash(JWT_USER_INFO_KEY)
public class JwtUserInfo {
    
    @Id
    private Long id;
    private String email;
    private String nickname;
    private String grade;
    
}
