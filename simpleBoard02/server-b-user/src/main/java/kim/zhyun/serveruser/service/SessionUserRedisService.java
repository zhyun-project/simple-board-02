package kim.zhyun.serveruser.service;

import kim.zhyun.serveruser.entity.SessionUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SessionUserRedisService {
    
    Optional<SessionUser> findById(String id);
    boolean existsById(String id);
    SessionUser save(SessionUser source);
    void deleteById(String id);

}
