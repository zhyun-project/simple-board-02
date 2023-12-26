package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.advice.NotFoundSessionException;
import kim.zhyun.serveruser.data.SessionUserEmailUpdate;
import kim.zhyun.serveruser.data.SessionUserNicknameUpdate;
import kim.zhyun.serveruser.entity.SessionUser;
import kim.zhyun.serveruser.repository.SessionUserRepository;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static kim.zhyun.serveruser.data.type.ExceptionType.NOT_FOUND_SESSION;

@Slf4j
@RequiredArgsConstructor
@Service
public class SessionUserServiceImpl implements SessionUserService {
    private final SessionUserRepository sessionUserRepository;
    
    @Override
    public SessionUser findById(String id) {
        Optional<SessionUser> optional = sessionUserRepository.findById(id);
        
        if (optional.isEmpty())
            throw new NotFoundSessionException(NOT_FOUND_SESSION);
        
        return optional.get();
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
        source.setEmail(update.getEmail());
        source.setEmailVerification(update.isEmailVerification());
        return save(source);
    }
    
    @Override
    public SessionUser updateNickname(SessionUserNicknameUpdate update) {
        SessionUser source = findById(update.getId());
        source.setNickname(update.getNickname());
        return save(source);
    }
    
    @Override
    public void deleteById(String id) {
        sessionUserRepository.deleteById(id);
    }
    
    
}
