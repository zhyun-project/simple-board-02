package kim.zhyun.serveruser.service.impl;

import kim.zhyun.jwt.data.JwtConstants;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.serveruser.advice.MemberException;
import kim.zhyun.serveruser.data.UserDto;
import kim.zhyun.serveruser.data.entity.User;
import kim.zhyun.serveruser.data.response.UserResponse;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static kim.zhyun.jwt.data.JwtConstants.JWT_PREFIX;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_NOT_FOUND;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_SIGNIN_FAIL;
import static org.springframework.data.domain.Sort.Order.asc;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProvider jwtProvider;
    private final JwtConstants jwtItems;
    
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
        redisTemplate.expire(jwt, jwtItems.expiredTime, jwtItems.expiredTimeUnit);
        
        SecurityContextHolder.clearContext();
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
