package kim.zhyun.serveruser.service;

import kim.zhyun.serveruser.entity.SessionUser;

import java.util.Optional;

public interface SessionUserService {
    
    Optional<SessionUser> findById(String id);
    boolean existsById(String id);
    SessionUser save(SessionUser source);
    void deleteById(String id);

}
