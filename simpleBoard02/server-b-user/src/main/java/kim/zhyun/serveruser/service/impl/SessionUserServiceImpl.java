package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.data.SessionUserEmailUpdate;
import kim.zhyun.serveruser.data.SessionUserNicknameUpdate;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.repository.SessionUserRepository;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class SessionUserServiceImpl implements SessionUserService {
    private final SessionUserRepository sessionUserRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${sign-up.key.session}")   private String KEY_SESSION_USER;
    @Value("${sign-up.key.email}")      private String KEY_EMAIL;
    @Value("${sign-up.key.nickname}")   private String KEY_NICKNAME;
    @Value("${sign-up.session.expire}") private long SESSION_EXPIRE_TIME;
    
    @Override
    public SessionUser findById(String id) {
        return sessionUserRepository.findById(id)
                .orElseGet(() -> sessionUserRepository.save(SessionUser.builder()
                        .sessionId(id).build()));
    }
    
    @Override
    public boolean existsById(String id) {
        return sessionUserRepository.existsById(id);
    }
    
    @Override
    public void save(SessionUser source) {
        sessionUserRepository.save(source);
    }
    
    @Override
    public void updateEmail(SessionUserEmailUpdate update) {
        SessionUser source = findById(update.getId());
        source.setEmail(update.getEmail().replace(KEY_EMAIL, ""));
        source.setEmailVerification(update.isEmailVerification());
        save(source);
    }
    
    @Override
    public void updateNickname(SessionUserNicknameUpdate update) {
        SessionUser source = findById(update.getId());
        source.setNickname(update.getNickname().replace(KEY_NICKNAME, ""));
        save(source);
    }
    
    @Override
    public void deleteById(String id) {
        SessionUser sessionUser = findById(id);
        
        String nickname = sessionUser.getNickname();
        if (nickname != null) {
            redisTemplate.delete(KEY_NICKNAME + nickname);
        }
        
        sessionUserRepository.deleteById(id);
    }
    
    @Override
    public void initSessionUserExpireTime(String id) {
        SessionUser sessionUser = findById(id);
        
        String nickname = sessionUser.getNickname();
        if (nickname != null) {
            redisTemplate.expire(KEY_NICKNAME + nickname, SESSION_EXPIRE_TIME, TimeUnit.MINUTES);
        }
        
        redisTemplate.expire(KEY_SESSION_USER + id, SESSION_EXPIRE_TIME, TimeUnit.MINUTES);
    }
    
}
