package kim.zhyun.serveruser.service;

import kim.zhyun.serveruser.data.SessionUserEmailUpdate;
import kim.zhyun.serveruser.data.SessionUserNicknameUpdate;
import kim.zhyun.serveruser.data.entity.SessionUser;

public interface SessionUserService {
    
    SessionUser findById(String id);
    boolean existsById(String id);
    void save(SessionUser source);
    void updateEmail(SessionUserEmailUpdate emailUpdate);
    void updateNickname(SessionUserNicknameUpdate emailUpdate);
    void deleteById(String id);
    void initSessionUserExpireTime(String id);
    
}
