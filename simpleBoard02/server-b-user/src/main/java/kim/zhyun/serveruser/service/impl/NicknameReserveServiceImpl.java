package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.data.NicknameDto;
import kim.zhyun.serveruser.entity.SessionUser;
import kim.zhyun.serveruser.repository.SessionUserRepository;
import kim.zhyun.serveruser.service.NicknameReserveService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Getter @Setter
@Component
public class NicknameReserveServiceImpl implements NicknameReserveService {
    private final RedisTemplate<String, String> template;
    private final SessionUserRepository sessionUserRepository;
    
    /**
     * 예약된 nickname인지 조회
     * - nickname 중복 확인 통과 후 다시 조회하는 경우, 먼저 예약 된 nickname 삭제
     */
    @Override
    public boolean existNickname(NicknameDto dto) {
        Optional<SessionUser> optionalSessionUser = sessionUserRepository.findById(dto.getSessionId());
        if (optionalSessionUser.isPresent()) {
            
            SessionUser sessionUser = optionalSessionUser.get();
            String userNickname = sessionUser.getNickname();

            if (userNickname != null && !userNickname.isBlank()) {
                deleteNickname(NicknameDto.of(userNickname));
                sessionUser.setNickname(null);
                sessionUserRepository.save(sessionUser);
            }
            
        }
        
        return template.hasKey(dto.getNickname());
    }
    
    /**
     * 사용 가능한 nickname인지 조회
     */
    @Override
    public boolean availableNickname(NicknameDto dto) {
        if (!existNickname(dto))
            return true;
        
        return template.opsForSet().isMember(dto.getNickname(), dto.getSessionId());
    }
    
    /**
     * nickname 예약
     */
    @Override
    public void saveNickname(NicknameDto dto) {
        deleteNickname(dto);
        template.opsForSet().add(dto.getNickname(), dto.getSessionId());
    }
    
    /**
     * nickname 삭제
     */
    @Override
    public void deleteNickname(NicknameDto dto) {
        template.delete(dto.getNickname());
    }
    
}
