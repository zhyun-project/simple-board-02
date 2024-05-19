package kim.zhyun.serveruser.domain.signup.converter;

import kim.zhyun.serveruser.domain.signup.business.model.SessionUserEmailUpdateDto;
import org.springframework.stereotype.Component;

@Component
public class SessionUserConverter {
    
    public SessionUserEmailUpdateDto toEmailUpdateDto(String sessionId, String email, boolean emailVerify) {
        return SessionUserEmailUpdateDto.builder()
                .id(sessionId)
                .emailVerification(emailVerify)
                .email(email)
                .build();
    }
}
