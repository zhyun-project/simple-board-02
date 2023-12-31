package kim.zhyun.serveruser.service.impl;

import kim.zhyun.serveruser.advice.MemberException;
import kim.zhyun.serveruser.data.UserDto;
import kim.zhyun.serveruser.data.entity.User;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.SIGNIN_FAIL;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {
    private final UserRepository userRepository;
    
    @Override
    public UserDto findByEmail(String email) {
        Optional<User> userContainer = userRepository.findByEmail(email);
        
        if (userContainer.isEmpty())
            throw new MemberException(SIGNIN_FAIL);
            
        return UserDto.from(userContainer.get());
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        Optional<User> userContainer = userRepository.findByEmail(username);
        
        if (userContainer.isEmpty())
            throw new MemberException(SIGNIN_FAIL);
        
        User user = userContainer.get();
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().getRole())
                .build();
    }
    
}
