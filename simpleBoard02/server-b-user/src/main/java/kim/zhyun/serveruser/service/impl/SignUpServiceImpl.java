package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.advice.MailAuthException;
import kim.zhyun.serveruser.data.EmailAuthCodeRequest;
import kim.zhyun.serveruser.data.NicknameDto;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.service.EmailService;
import kim.zhyun.serveruser.service.NicknameReserveService;
import kim.zhyun.serveruser.service.SessionUserService;
import kim.zhyun.serveruser.service.SignUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static kim.zhyun.serveruser.data.type.ExceptionType.REQUIRE_MAIL_DUPLICATE_CHECK;

@RequiredArgsConstructor
@Service
public class SignUpServiceImpl implements SignUpService {
    private final UserRepository userRepository;
    private final NicknameReserveService nicknameReserveService;
    private final SessionUserService sessionUserService;
    private final EmailService emailService;
    
    @Override
    public boolean availableEmail(String email, String sessionId) {
        boolean existEmail = userRepository.existsByEmailIgnoreCase(email);
        
        if (existEmail)
            return false;
        
        // session user 저장소에 이메일 등록
        saveEmailToSessionUserStorage(email, sessionId);
        
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
        saveNicknameToSessionUserStorage(nickname, sessionId);
        
        return true;
    }
    
    @Override
    public void sendEmailAuthCode(String sessionId, EmailAuthCodeRequest request) {
        // 1. email 중복검사 확인
        SessionUser sessionUser = sessionUserService.findById(sessionId);
        
        if (sessionUser.getEmail() == null || !sessionUser.getEmail().equals(request.getEmail()))
            throw new MailAuthException(REQUIRE_MAIL_DUPLICATE_CHECK);
        
        // 2. 메일 발송
        emailService.sendEmailAuthCode(request.getEmail());
    }
    
    /**
     * session user 저장소에 이메일 등록
     */
    private void saveEmailToSessionUserStorage(String email, String sessionId) {
        SessionUser sessionUser = sessionUserService.findById(sessionId);
        sessionUser.setEmail(email);
        sessionUserService.save(sessionUser);
    }
    
    /**
     * session user 저장소에 닉네임 등록
     */
    private void saveNicknameToSessionUserStorage(String nickname, String sessionId) {
        SessionUser sessionUser = sessionUserService.findById(sessionId);
        sessionUser.setNickname(nickname);
        sessionUserService.save(sessionUser);
    }
    
}
