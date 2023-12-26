package kim.zhyun.serveruser.service;

import kim.zhyun.serveruser.data.SessionUserEmailUpdate;
import kim.zhyun.serveruser.data.SessionUserNicknameUpdate;
import kim.zhyun.serveruser.entity.SessionUser;

import java.util.Optional;

public interface SessionUserService {
    
    SessionUser findById(String id);
    boolean existsById(String id);
    SessionUser save(SessionUser source);
    SessionUser updateEmail(SessionUserEmailUpdate emailUpdate);
    SessionUser updateNickname(SessionUserNicknameUpdate emailUpdate);
    void deleteById(String id);

}
