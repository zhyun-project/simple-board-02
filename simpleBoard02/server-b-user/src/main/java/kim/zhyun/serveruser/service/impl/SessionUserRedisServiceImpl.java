package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.entity.SessionUser;
import kim.zhyun.serveruser.repository.SessionUserRedisRepository;
import kim.zhyun.serveruser.service.SessionUserRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SessionUserRedisServiceImpl implements SessionUserRedisService {
    private final SessionUserRedisRepository sessionUserRedisRepository;
    
    @Override
    public Optional<SessionUser> findById(String id) {
        return sessionUserRedisRepository.findById(id);
    }
    
    @Override
    public boolean existsById(String id) {
        return sessionUserRedisRepository.existsById(id);
    }
    
    @Override
    public SessionUser save(SessionUser source) {
        return sessionUserRedisRepository.save(source);
    }
    
    @Override
    public void deleteById(String id) {
        sessionUserRedisRepository.deleteById(id);
    }
    
    
}
