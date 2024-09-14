package kim.zhyun.jwt.domain.service;

import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class JwtLogoutService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProvider jwtProvider;
    
    public boolean isLogoutToken(String token) {
        token = token.trim();
        return redisTemplate.hasKey(token);
    }
    
    public void setLogoutToken(String token, JwtUserInfoDto jwtUserInfoDto) {
        token = token.trim();
        redisTemplate.opsForSet().add(token, jwtUserInfoDto.getEmail());
        redisTemplate.expire(token, jwtProvider.expiredTime, jwtProvider.expiredTimeUnit);
    }
    
}
