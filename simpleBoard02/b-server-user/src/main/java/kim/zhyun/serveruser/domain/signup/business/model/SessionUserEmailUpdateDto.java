package kim.zhyun.serveruser.domain.signup.business.model;

import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter @Builder
public class SessionUserEmailUpdateDto {
    
    private String id;
    private String email;
    private boolean emailVerification;
    
}
