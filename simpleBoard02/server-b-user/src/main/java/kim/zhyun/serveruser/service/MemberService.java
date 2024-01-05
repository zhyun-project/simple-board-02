package kim.zhyun.serveruser.service;

import kim.zhyun.serveruser.data.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface MemberService extends UserDetailsService {
    UserDto findByEmail(String email);
    void logout(String token, String email);
    
}
