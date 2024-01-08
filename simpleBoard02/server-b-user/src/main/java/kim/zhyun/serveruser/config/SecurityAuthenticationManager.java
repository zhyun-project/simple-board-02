package kim.zhyun.serveruser.config;

import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.serveruser.advice.MemberException;
import kim.zhyun.serveruser.data.UserDto;
import kim.zhyun.serveruser.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_SIGNIN_FAIL;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_WITHDRAWAL;
import static kim.zhyun.serveruser.data.type.RoleType.TYPE_WITHDRAWAL;

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
            throw new MemberException(EXCEPTION_SIGNIN_FAIL);
        
        if (userDto.isWithdrawal()) {
            Map<String, Long> dateTime = withdrawalDateTimeMap(userDto.getModifiedAt());
            throw new MemberException(String.format(EXCEPTION_WITHDRAWAL,
                    dateTime.get("d"),
                    dateTime.get("h"),
                    dateTime.get("m")));
        }
        
        return new UsernamePasswordAuthenticationToken(
                JwtUserDto.builder()
                        .id(userDto.getId())
                        .email(userDto.getEmail())
                        .nickname(userDto.getNickname()).build(),
                userDto.getPassword(),
                authentication.getAuthorities());
    }
    
    
    
    private Map<String, Long> withdrawalDateTimeMap(LocalDateTime withdrawalAt) {
        LocalDateTime nowAt = LocalDateTime.now();
        
        LocalDate withdrawalDate = withdrawalAt.toLocalDate();
        LocalTime withdrawalTime = withdrawalAt.toLocalTime();
        
        LocalDate nowDate = nowAt.toLocalDate();
        LocalTime nowTime = nowAt.toLocalTime();
        
        Map<String, Long> dateTime = new HashMap<>();
        dateTime.put("d", ChronoUnit.DAYS.between(withdrawalDate, nowDate));
        dateTime.put("h", ChronoUnit.HOURS.between(withdrawalTime, nowTime));
        dateTime.put("m", ChronoUnit.MINUTES.between(withdrawalTime, nowTime));
        
        return dateTime;
    }
    
}
