package kim.zhyun.serveruser.domain.signup.converter;

import kim.zhyun.serveruser.domain.signup.controller.model.dto.NicknameFindDto;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.NicknameUpdateDto;
import org.springframework.stereotype.Component;

@Component
public class NicknameReserveConverter {
    
    public NicknameFindDto toFindDto (String sessionId, String nickname) {
        return NicknameFindDto.builder()
                .sessionId(sessionId)
                .nickname(nickname)
                .build();
    }
    
    public NicknameUpdateDto toUpdateDto (String sessionId, String nickname) {
        return NicknameUpdateDto.builder()
                .id(sessionId)
                .nickname(nickname)
                .build();
    }
}
