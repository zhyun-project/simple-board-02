package kim.zhyun.serveruser.domain.signup.controller.model.dto;

import lombok.*;

import java.util.Objects;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NicknameFindDto {
    
    private String nickname;
    
    @Getter
    private String sessionId;
    
    
    
    public String getNickname() {
        return "NICKNAME:" + nickname;
    }
    
    
    
    public static NicknameFindDto of(String nickname) {
        return new NicknameFindDto(nickname, null);
    }
    
    
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        NicknameFindDto that = (NicknameFindDto) obj;
        
        if (!Objects.equals(sessionId, that.sessionId)) return false;
        return Objects.equals(nickname, that.nickname);
    }
    
}
