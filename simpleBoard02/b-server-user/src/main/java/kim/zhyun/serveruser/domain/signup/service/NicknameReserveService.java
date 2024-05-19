package kim.zhyun.serveruser.domain.signup.service;

import kim.zhyun.serveruser.domain.member.service.SessionUserService;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.NicknameFindDto;
import kim.zhyun.serveruser.domain.signup.converter.NicknameReserveConverter;
import kim.zhyun.serveruser.domain.signup.repository.SessionUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Getter
@Setter
@Service
public class NicknameReserveService {
    
    private final RedisTemplate<String, String> template;
    private final SessionUserService sessionUserService;
    private final NicknameReserveConverter nicknameReserveConverter;
    
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
    public boolean availableNickname(NicknameFindDto dto) {
        
        if (isMyReservedNickname(dto))
            return true;
        
        SessionUser sessionUser = sessionUserService.findById(dto.getSessionId());
        String userNickname = sessionUser.getNickname();
        
        if (userNickname != null) {
            deleteNickname(NicknameFindDto.of(userNickname));
            sessionUserUpdateNickname(dto.getSessionId(), "");
        }
        
        return !template.hasKey(dto.getNickname());
    }
    
    /**
     * nickname 예약
     */
    public void saveNickname(NicknameFindDto dto) {
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
    public void deleteNickname(NicknameFindDto dto) {
        template.delete(dto.getNickname());
    }
    
    
    private boolean isMyReservedNickname(NicknameFindDto dto) {
        return template.opsForSet().isMember(dto.getNickname(), dto.getSessionId());
    }
    
    private void sessionUserUpdateNickname(String sessionId, String nickname) {
        sessionUserService.updateNickname(
                nicknameReserveConverter.toUpdateDto(sessionId, nickname)
        );
    }
    
}
