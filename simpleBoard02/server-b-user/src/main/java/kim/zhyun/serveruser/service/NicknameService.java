package kim.zhyun.serveruser.service;

public interface NicknameService {
    
    boolean existNickname(String nickname, String sessionId);
    boolean availableNickname(String nickname, String sessionId);
    void saveNickname(String nickname, String sessionId);
    void deleteNickname(String nickname);
    
}
