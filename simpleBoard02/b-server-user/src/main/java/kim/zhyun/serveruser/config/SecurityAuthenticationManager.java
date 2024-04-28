package kim.zhyun.serveruser.config;

import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.serveruser.advice.MemberException;
import kim.zhyun.serveruser.config.model.UserDto;
import kim.zhyun.serveruser.domain.member.converter.UserConverter;
import kim.zhyun.serveruser.domain.member.service.MemberService;
import kim.zhyun.serveruser.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_SIGNIN_FAIL;
import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_WITHDRAWAL;
import static kim.zhyun.serveruser.utils.DateTimeUtil.dateTimeCalculate;

@RequiredArgsConstructor
@Component
public class SecurityAuthenticationManager implements AuthenticationManager {
    private final MemberService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserConverter userConverter;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = (String) authentication.getCredentials();
        UserDto userDto = userConverter.toDto(userService.findByEmailWithThrow(email));
        
        if (!passwordEncoder.matches(password, userDto.getPassword()))
            throw new MemberException(EXCEPTION_SIGNIN_FAIL);
        
        if (userDto.isWithdrawal()) {
            DateTimeUtil.DateTimePeriodDto dateTimePeriodDto = dateTimeCalculate(userDto.getModifiedAt());
            throw new MemberException(String.format(
                    EXCEPTION_WITHDRAWAL,
                    dateTimePeriodDto.days(),
                    dateTimePeriodDto.hours(),
                    dateTimePeriodDto.minutes()));
        }
        
        return new UsernamePasswordAuthenticationToken(
                JwtUserInfoDto.builder()
                        .id(userDto.getId())
                        .email(userDto.getEmail())
                        .nickname(userDto.getNickname())
                        .build(),
                userDto.getPassword(),
                Set.of(new SimpleGrantedAuthority("ROLE_" + userDto.getRole().getGrade())));
    }
    
}
