package kim.zhyun.serveruser.config;

import kim.zhyun.serveruser.advice.MemberException;
import kim.zhyun.serveruser.data.UserDto;
import kim.zhyun.serveruser.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.SIGNIN_FAIL;

@RequiredArgsConstructor
@Component
public class SecurityAuthenticationManager implements AuthenticationManager {
    private final MemberService userService;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = (String) authentication.getCredentials();
        UserDto userDto = userService.findByEmail(email);
        
        if (!passwordEncoder.matches(password, userDto.getPassword()))
            throw new MemberException(SIGNIN_FAIL);
        
        return new UsernamePasswordAuthenticationToken(
                userDto.getEmail(),
                userDto.getPassword(),
                Set.of(new SimpleGrantedAuthority(userDto.getRole().getGrade())));
    }
    
}
