package kim.zhyun.serveruser.service;

public interface SignUpService {
    boolean availableEmail(String email, String sessionId);
    boolean availableNickname(String nickname, String sessionId);
}
