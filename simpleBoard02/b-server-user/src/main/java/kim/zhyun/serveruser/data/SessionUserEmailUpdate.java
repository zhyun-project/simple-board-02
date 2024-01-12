package kim.zhyun.serveruser.data;

import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter @Builder
public class SessionUserEmailUpdate {
    
    private String id;
    private String email;
    private boolean emailVerification;
    
}
