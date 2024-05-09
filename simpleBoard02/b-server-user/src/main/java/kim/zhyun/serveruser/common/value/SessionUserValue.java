package kim.zhyun.serveruser.common.value;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SessionUserValue {
    
    public String KEY_SESSION_USER;
    public String KEY_EMAIL;
    public String KEY_NICKNAME;
    public long SESSION_EXPIRE_TIME;
    
    public SessionUserValue(
            @Value("${sign-up.key.session}")    String KEY_SESSION_USER,
            @Value("${sign-up.key.email}")      String KEY_EMAIL,
            @Value("${sign-up.key.nickname}")   String KEY_NICKNAME,
            @Value("${sign-up.session.expire}") long SESSION_EXPIRE_TIME
    ) {
        this.KEY_SESSION_USER = KEY_SESSION_USER;
        this.KEY_EMAIL = KEY_EMAIL;
        this.KEY_NICKNAME = KEY_NICKNAME;
        this.SESSION_EXPIRE_TIME = SESSION_EXPIRE_TIME;
    }
    
}
