package kim.zhyun.serveruser.service;

import kim.zhyun.serveruser.data.UserDto;
import kim.zhyun.serveruser.data.UserGradeUpdateRequest;
import kim.zhyun.serveruser.data.UserUpdateRequest;
import kim.zhyun.serveruser.data.response.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface MemberService extends UserDetailsService {
    List<UserResponse> findAll();
    UserResponse findById(long id);
    UserDto findByEmail(String email);
    void logout(String token, String email);
    UserResponse updateUserInfo(String sessionId, UserUpdateRequest request);
    UserResponse updateUserGrade(UserGradeUpdateRequest request);
    
}
