package kim.zhyun.serveruser.service;

import kim.zhyun.serveruser.data.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface MemberService extends UserDetailsService {
    List<UserDto> findAll();
    UserDto findByEmail(String email);
    void logout(String token, String email);
    
}
