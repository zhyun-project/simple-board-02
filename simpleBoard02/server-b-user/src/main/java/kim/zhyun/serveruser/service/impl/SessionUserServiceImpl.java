package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.data.SessionUserEmailUpdate;
import kim.zhyun.serveruser.data.SessionUserNicknameUpdate;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.repository.SessionUserRepository;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SessionUserServiceImpl implements SessionUserService {
    private final SessionUserRepository sessionUserRepository;
    
    @Value("${sign-up.key.email}")      private String KEY_EMAIL;
    @Value("${sign-up.key.nickname}")   private String KEY_NICKNAME;
    
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
    public SessionUser save(SessionUser source) {
        return sessionUserRepository.save(source);
    }
    
    @Override
    public SessionUser updateEmail(SessionUserEmailUpdate update) {
        SessionUser source = findById(update.getId());
        source.setEmail(update.getEmail().replace(KEY_EMAIL, ""));
        source.setEmailVerification(update.isEmailVerification());
        return save(source);
    }
    
    @Override
    public SessionUser updateNickname(SessionUserNicknameUpdate update) {
        SessionUser source = findById(update.getId());
        source.setNickname(update.getNickname().replace(KEY_NICKNAME, ""));
        return save(source);
    }
    
    @Override
    public void deleteById(String id) {
        sessionUserRepository.deleteById(id);
    }
    
    
}
