package kim.zhyun.serveruser.data;

import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter @Builder
public class SessionUserNicknameUpdate {
    
    private String id;
    private String nickname;
    
}
