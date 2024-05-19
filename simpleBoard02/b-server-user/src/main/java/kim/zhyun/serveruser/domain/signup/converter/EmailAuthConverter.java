package kim.zhyun.serveruser.domain.signup.converter;

import kim.zhyun.serveruser.domain.signup.controller.model.EmailAuthCodeRequest;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.EmailAuthDto;
import org.springframework.stereotype.Component;

@Component
public class EmailAuthConverter {
    
    public EmailAuthDto toDto(String email, String code) {
        return EmailAuthDto.builder()
                .email(email)
                .code(code)
                .build();
    }
    
    public EmailAuthDto toDto(EmailAuthCodeRequest request, String code) {
        return EmailAuthDto.builder()
                .email(request.getEmail())
                .code(code)
                .build();
    }
}
