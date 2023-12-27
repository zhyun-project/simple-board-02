package kim.zhyun.serveruser.service;

import kim.zhyun.serveruser.data.EmailAuthDto;

public interface EmailService {
    
    boolean existEmail(EmailAuthDto dto);
    void sendEmailAuthCode(String userEmail);
    boolean existCode(EmailAuthDto dto);
    void saveEmailAuthCode(EmailAuthDto dto);
    void deleteAndUpdateSessionUserEmail(EmailAuthDto dto, String sessionId);
    
}
