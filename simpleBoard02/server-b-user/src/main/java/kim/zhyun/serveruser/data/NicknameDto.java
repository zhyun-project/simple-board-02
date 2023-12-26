package kim.zhyun.serveruser.data;

import kim.zhyun.serveruser.entity.SessionUser;
import lombok.*;

import java.util.Objects;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NicknameDto {
    
    private String nickname;
    
    @Getter
    private String sessionId;
    
    public String getNickname() {
        return "NICKNAME:" + nickname;
    }
    
    public static NicknameDto of(String nickname) {
        return new NicknameDto(nickname, null);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        NicknameDto that = (NicknameDto) obj;
        
        if (!Objects.equals(sessionId, that.sessionId)) return false;
        return Objects.equals(nickname, that.nickname);
    }
    
}
