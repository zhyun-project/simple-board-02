package kim.zhyun.jwt.domain.repository;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Objects;

import static kim.zhyun.jwt.constants.JwtConstants.JWT_USER_INFO_KEY;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
@RedisHash(JWT_USER_INFO_KEY)
public class JwtUserInfoEntity {
    
    @Id
    private Long id;
    private String email;
    private String nickname;
    private String grade;
    
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JwtUserInfoEntity that)) return false;
        return Objects.equals(getId(), that.getId())
                && Objects.equals(getEmail(), that.getEmail())
                && Objects.equals(getNickname(), that.getNickname())
                && Objects.equals(getGrade(), that.getGrade());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEmail(), getNickname(), getGrade());
    }
    
}
