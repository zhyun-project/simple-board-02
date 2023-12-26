package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.data.NicknameDto;
import kim.zhyun.serveruser.entity.SessionUser;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.service.NicknameReserveService;
import kim.zhyun.serveruser.service.SessionUserService;
import kim.zhyun.serveruser.service.SignUpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SignUpServiceImpl implements SignUpService {
    private final UserRepository userRepository;
    private final NicknameReserveService nicknameReserveService;
    private final SessionUserService sessionUserService;
    
    @Override
    public boolean availableEmail(String email, String sessionId) {
        boolean existEmail = userRepository.existsByEmailIgnoreCase(email);
        
        if (existEmail)
            return false;
        
        // session user 저장소에 이메일 등록
        SessionUser sessionUser = sessionUserService.findById(sessionId);
        sessionUser.setEmail(email);
        sessionUserService.save(sessionUser);
        return true;
    }
    
    @Override
    public boolean availableNickname(String nickname, String sessionId) {
        boolean isMemberUsed = userRepository.existsByNicknameIgnoreCase(nickname);
        
        if (isMemberUsed)
            return false;
        
        NicknameDto newNicknameInfo = NicknameDto.builder()
                .nickname(nickname)
                .sessionId(sessionId).build();
        
        boolean isReserved = nicknameReserveService.existNickname(newNicknameInfo);
        
        if (isReserved)
            return false;
        
        // 닉네임 예약
        nicknameReserveService.saveNickname(newNicknameInfo);
        
        // session user 저장소에 닉네임 등록
        SessionUser sessionUser = sessionUserService.findById(sessionId);
        sessionUser.setNickname(nickname);
        sessionUserService.save(sessionUser);
        return true;
    }
    
}
