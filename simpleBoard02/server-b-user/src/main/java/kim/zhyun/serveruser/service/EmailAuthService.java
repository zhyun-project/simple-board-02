package kim.zhyun.serveruser.service;

import kim.zhyun.serveruser.data.EmailAuthDto;

public interface EmailAuthService {
    
    boolean existEmail(EmailAuthDto dto);
    boolean existCode(EmailAuthDto dto);
    void saveEmailAuthCode(EmailAuthDto dto);
    void deleteAndUpdateSessionUserEmail(EmailAuthDto dto, String sessionId);
    
}
