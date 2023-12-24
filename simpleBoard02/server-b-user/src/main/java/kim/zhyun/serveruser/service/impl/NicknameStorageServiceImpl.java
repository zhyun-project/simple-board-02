package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.entity.SessionUser;
import kim.zhyun.serveruser.repository.SessionUserRedisRepository;
import kim.zhyun.serveruser.service.NicknameStorageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Getter @Setter
@Component
public class NicknameStorageServiceImpl implements NicknameStorageService {
    private final RedisTemplate<String, String> template;
    private final SessionUserRedisRepository sessionUserRedisRepository;
    
    /**
     * 예약된 nickname인지 조회
     * - nickname 중복 확인 통과 후 다시 조회하는 경우, 먼저 예약 된 nickname 삭제
     */
    @Override
    public boolean existNickname(String nickname, String sessionId) {
        Optional<SessionUser> optionalSessionUser = sessionUserRedisRepository.findById(sessionId);
        if (optionalSessionUser.isPresent()) {
            
            SessionUser sessionUser = optionalSessionUser.get();
            String userNickname = sessionUser.getNickname();

            if (userNickname != null && !userNickname.isBlank()) {
                deleteNickname(userNickname);
                sessionUser.setNickname(null);
                sessionUserRedisRepository.save(sessionUser);
            }
            
        }
        
        return template.hasKey(nickname);
    }
    
    /**
     * 사용 가능한 nickname인지 조회
     */
    @Override
    public boolean availableNickname(String nickname, String sessionId) {
        if (!existNickname(nickname, sessionId))
            return true;
        
        return template.opsForSet().isMember(nickname, sessionId);
    }
    
    /**
     * nickname 예약
     */
    @Override
    public void saveNickname(String nickname, String sessionId) {
        deleteNickname(nickname);
        template.opsForSet().add(nickname, sessionId);
    }
    
    /**
     * nickname 삭제
     */
    @Override
    public void deleteNickname(String nickname) {
        template.delete(nickname);
    }
    
}
