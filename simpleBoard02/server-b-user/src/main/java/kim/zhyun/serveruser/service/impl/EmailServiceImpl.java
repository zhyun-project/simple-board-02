package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.data.EmailAuthDto;
import kim.zhyun.serveruser.data.SessionUserEmailUpdate;
import kim.zhyun.serveruser.service.EmailService;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailServiceImpl implements EmailService {
    private final RedisTemplate<String, String> template;
    private final SessionUserService sessionUserService;
    
    @Value("${sign-up.email.expire}")   private long expireTime;
    
    @Override
    public boolean existEmail(EmailAuthDto dto) {
        return template.hasKey(dto.getEmail());
    }
    
    @Override
    public boolean existCode(EmailAuthDto dto) {
        return template.opsForSet().isMember(dto.getEmail(), dto.getCode());
    }
    
    @Override
    public void saveEmailAuthCode(EmailAuthDto dto) {
        template.opsForSet().add(dto.getEmail(), dto.getCode());
        template.expire(dto.getEmail(), expireTime, SECONDS);
    }
    
    @Override
    public void deleteAndUpdateSessionUserEmail(EmailAuthDto dto, String sessionId) {
        SessionUserEmailUpdate sessionUserEmailUpdate = SessionUserEmailUpdate.builder()
                .id(sessionId)
                .email(dto.getEmail())
                .emailVerification(true).build();
        
        template.delete(dto.getEmail());
        sessionUserService.updateEmail(sessionUserEmailUpdate);
    }
    
}
