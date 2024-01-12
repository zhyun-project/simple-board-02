package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.data.NicknameDto;
import kim.zhyun.serveruser.data.SessionUserNicknameUpdate;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.service.NicknameReserveService;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Getter @Setter
@Component
public class NicknameReserveServiceImpl implements NicknameReserveService {
    private final RedisTemplate<String, String> template;
    private final SessionUserService sessionUserService;
    
    @Value("${sign-up.session.expire}")
    private long SESSION_EXPIRE_TIME;
    
    /**
     * 사용 가능한 nickname인지 조회
     * - nickname 중복 확인 통과 후 다시 조회하는 경우
     *   - 조회하는 닉네임이 자신이 예약한 닉네임 O 
     *     - true 반환
     *   - 조회하는 닉네임이 자신이 예약한 닉네임 X 
     *     - 자신이 예약했던 nickname 삭제
     *     - 조회하는 닉네임의 예약 상태 반환
     */
    @Override
    public boolean availableNickname(NicknameDto dto) {

        if (isMyReservedNickname(dto))
            return true;
        
        SessionUser sessionUser = sessionUserService.findById(dto.getSessionId());
        String userNickname = sessionUser.getNickname();
        
        if (userNickname != null) {
            deleteNickname(NicknameDto.of(userNickname));
            sessionUserUpdateNickname(dto.getSessionId(), "");
        }
        
        return !template.hasKey(dto.getNickname());
    }
    
    /**
     * nickname 예약
     */
    @Override
    public void saveNickname(NicknameDto dto) {
        if (!isMyReservedNickname(dto)) {
            
            deleteNickname(dto);
            
            template.opsForSet().add(dto.getNickname(), dto.getSessionId());
            template.expire(dto.getNickname(), SESSION_EXPIRE_TIME, TimeUnit.MINUTES);
            
            sessionUserUpdateNickname(dto.getSessionId(), dto.getNickname());
        }
    }
    
    /**
     * nickname 삭제
     */
    @Override
    public void deleteNickname(NicknameDto dto) {
        template.delete(dto.getNickname());
    }
    
    
    private boolean isMyReservedNickname(NicknameDto dto) {
        return template.opsForSet().isMember(dto.getNickname(), dto.getSessionId());
    }
    
    private void sessionUserUpdateNickname(String sessionId, String nickname) {
        sessionUserService.updateNickname(SessionUserNicknameUpdate.builder()
                .id(sessionId)
                .nickname(nickname).build());
    }
}
