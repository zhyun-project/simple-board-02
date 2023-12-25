package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.entity.SessionUser;
import kim.zhyun.serveruser.repository.SessionUserRepository;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SessionUserServiceImpl implements SessionUserService {
    private final SessionUserRepository sessionUserRepository;
    
    @Override
    public Optional<SessionUser> findById(String id) {
        return sessionUserRepository.findById(id);
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
    public void deleteById(String id) {
        sessionUserRepository.deleteById(id);
    }
    
    
}
