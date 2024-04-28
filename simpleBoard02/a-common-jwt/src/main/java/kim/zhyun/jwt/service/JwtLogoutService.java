package kim.zhyun.jwt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class JwtLogoutService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isLogoutToken(String token, String email) {
        if (!redisTemplate.hasKey(token))
            return false;
        
        return redisTemplate.opsForSet().isMember(token, email);
    }
    
}
