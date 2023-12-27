package kim.zhyun.serveruser.service;

import kim.zhyun.serveruser.data.EmailAuthCodeRequest;

public interface SignUpService {
    boolean availableEmail(String email, String sessionId);
    boolean availableNickname(String nickname, String sessionId);
    void sendEmailAuthCode(String sessionId, EmailAuthCodeRequest request);
    void verifyEmailAuthCode(String sessionId, String code);
}
