package kim.zhyun.serveruser.domain.member.service;

import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.serveruser.common.value.SessionUserValue;
import kim.zhyun.serveruser.domain.signup.business.model.SessionUserEmailUpdateDto;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.NicknameUpdateDto;
import kim.zhyun.serveruser.domain.signup.repository.SessionUser;
import kim.zhyun.serveruser.domain.signup.repository.SessionUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_REQUIRE_MAIL_DUPLICATE_CHECK;
import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK;

@Slf4j
@RequiredArgsConstructor
@Service
public class SessionUserService {
    private final SessionUserRepository sessionUserRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    private final SessionUserValue value;
    
    
    public boolean existNicknameDuplicateCheckWithThrow(String id, String nickname) {
        SessionUser sessionUser = findById(id);
        
        // 닉네임 중복확인 체크
        if (sessionUser.getNickname() == null
                || !sessionUser.getNickname().equals(nickname))
            throw new ApiException(EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK);
        
        return true;
    }
    
    public void emailDuplicateCheckWithThrow(String id, String email) {
        SessionUser sessionUser = findById(id);
        
        // email 중복확인 체크
        if (sessionUser.getEmail() == null || !sessionUser.getEmail().equals(email))
            throw new ApiException(EXCEPTION_REQUIRE_MAIL_DUPLICATE_CHECK);
        
    }
    
    public SessionUser findById(String id) {
        return sessionUserRepository.findById(id)
                .orElseGet(() -> sessionUserRepository.save(SessionUser.builder().sessionId(id).build()));
    }
    
    public boolean existsById(String id) {
        return sessionUserRepository.existsById(id);
    }
    
    public void save(SessionUser source) {
        sessionUserRepository.save(source);
    }
    
    public void updateEmail(SessionUserEmailUpdateDto update) {
        SessionUser source = findById(update.getId());
        source.setEmail(update.getEmail().replace(value.KEY_EMAIL, ""));
        source.setEmailVerification(update.isEmailVerification());
        save(source);
    }
    
    public void updateNickname(NicknameUpdateDto update) {
        SessionUser source = findById(update.getId());
        source.setNickname(update.getNickname().replace(value.KEY_NICKNAME, ""));
        save(source);
    }
    
    public void deleteById(String id) {
        sessionUserRepository.deleteById(id);
    }
    
    public void initSessionUserExpireTime(String id) {
        SessionUser sessionUser = findById(id);
        
        String nickname = sessionUser.getNickname();
        if (nickname != null) {
            redisTemplate.expire(value.KEY_NICKNAME + nickname, value.SESSION_EXPIRE_TIME, TimeUnit.MINUTES);
        }
        
        redisTemplate.expire(value.KEY_SESSION_USER + id, value.SESSION_EXPIRE_TIME, TimeUnit.MINUTES);
    }
    
    
}
