package kim.zhyun.serveruser.domain.signup.controller.model.dto;

import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter @Builder
public class NicknameUpdateDto {
    
    private String id;
    private String nickname;
    
}
