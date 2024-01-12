package kim.zhyun.serveruser.service.impl;

import io.netty.util.internal.StringUtil;
import kim.zhyun.jwt.data.JwtUserInfo;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.jwt.repository.JwtUserInfoRepository;
import kim.zhyun.serveruser.advice.MemberException;
import kim.zhyun.serveruser.data.UserDto;
import kim.zhyun.serveruser.data.UserGradeUpdateRequest;
import kim.zhyun.serveruser.data.UserUpdateRequest;
import kim.zhyun.serveruser.data.entity.Role;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.data.entity.User;
import kim.zhyun.serveruser.data.response.UserResponse;
import kim.zhyun.serveruser.repository.RoleRepository;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.service.MemberService;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static kim.zhyun.jwt.data.JwtConstants.JWT_PREFIX;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.*;
import static kim.zhyun.serveruser.data.type.RoleType.TYPE_WITHDRAWAL;
import static org.springframework.data.domain.Sort.Order.asc;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final SessionUserService sessionUserService;
    private final JwtUserInfoRepository jwtUserInfoRepository;
    private final RoleRepository roleRepository;
    
    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll(Sort.by(asc("id")))
                .stream()
                .map(UserResponse::from)
                .toList();
    }
    
    @Override
    public UserResponse findById(long id) {
        Optional<User> userContainer = userRepository.findById(id);
        
        if (userContainer.isEmpty())
            throw new MemberException(EXCEPTION_NOT_FOUND);
        
        return UserResponse.from(userContainer.get());
    }
    
    @Override
    public UserDto findByEmail(String email) {
        Optional<User> userContainer = userRepository.findByEmail(email);
        
        if (userContainer.isEmpty())
            throw new MemberException(EXCEPTION_SIGNIN_FAIL);
            
        return UserDto.from(userContainer.get());
    }
    
    @Override
    public void logout(String token, String email) {
        String jwt = token.substring(JWT_PREFIX.length());
        
        redisTemplate.opsForSet().add(jwt, email);
        redisTemplate.expire(jwt, jwtProvider.expiredTime, jwtProvider.expiredTimeUnit);
        
        SecurityContextHolder.clearContext();
    }
    
    @Override
    public UserResponse updateUserInfo(String sessionId, UserUpdateRequest request) {
        Optional<User> userContainer = userRepository.findById(request.getId());
        
        if (userContainer.isEmpty())
            throw new MemberException(EXCEPTION_NOT_FOUND);
        
        User user = userContainer.get();
        
        // 닉네임 업데이트
        if (!StringUtil.isNullOrEmpty(request.getNickname()) && !request.getNickname().equals(user.getNickname())) {
            SessionUser sessionUser = sessionUserService.findById(sessionId);
            
            // 닉네임 중복확인 체크
            if (sessionUser.getNickname() == null
                    || !sessionUser.getNickname().equals(request.getNickname()))
                throw new MemberException(EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK);
            
            user.setNickname(request.getNickname());
        }
        
        // 비밀번호 업데이트
        if (!StringUtil.isNullOrEmpty(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        sessionUserService.deleteById(sessionId);
        
        User saved = userRepository.save(user);
        jwtUserInfoUpdate(saved);
        
        return UserResponse.from(saved);
    }
    
    @Override
    public UserResponse updateUserGrade(UserGradeUpdateRequest request) {
        Optional<User> userContainer = userRepository.findById(request.getId());
        
        if (userContainer.isEmpty())
            throw new MemberException(EXCEPTION_NOT_FOUND);
        
        User user = userContainer.get();
        String roleType = request.getRole().toUpperCase();
        
        if (user.getRole().getGrade().equals(TYPE_WITHDRAWAL) && roleType.equals(TYPE_WITHDRAWAL))
            throw new MemberException(EXCEPTION_ALREADY_WITHDRAWN_MEMBER);
        
        userSetRole(user, roleType);
        
        User saved = userRepository.save(user);
        jwtUserInfoUpdate(saved);
        return UserResponse.from(saved);
    }
    
    @Override
    public UserDto withdrawal(String jwt) {
        String token = jwt.substring(JWT_PREFIX.length());
        Optional<User> userContainer = userRepository.findById(jwtProvider.idFrom(token));
        
        if (userContainer.isEmpty())
            throw new MemberException(EXCEPTION_NOT_FOUND);
        
        User user = userContainer.get();
        userSetRole(user, TYPE_WITHDRAWAL);
        
        User updated = userRepository.save(user);
        jwtUserInfoUpdate(updated);
        
        return UserDto.from(updated);
    }

    
    /**
     * 회원 권한 설정
     */
    private void userSetRole(User user, String roleType) {
        Role role = roleRepository.findByGrade(roleType);
        user.setRole(role);
        user.setWithdrawal(TYPE_WITHDRAWAL.equals(roleType));
    }
    
    /**
     * redis user info 저장소 업데이트
     */
    private void jwtUserInfoUpdate(User user) {
        jwtUserInfoRepository.save(JwtUserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .grade("ROLE_" + user.getRole().getGrade())
                .build());
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        Optional<User> userContainer = userRepository.findByEmail(username);
        
        if (userContainer.isEmpty())
            throw new MemberException(EXCEPTION_SIGNIN_FAIL);
        
        User user = userContainer.get();
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().getGrade())
                .build();
    }
    
}
