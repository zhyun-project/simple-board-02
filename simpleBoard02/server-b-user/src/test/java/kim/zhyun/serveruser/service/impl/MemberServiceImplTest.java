package kim.zhyun.serveruser.service.impl;

import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.serveruser.advice.MemberException;
import kim.zhyun.serveruser.config.SecurityAuthenticationManager;
import kim.zhyun.serveruser.config.SecurityConfig;
import kim.zhyun.serveruser.data.SignInRequest;
import kim.zhyun.serveruser.data.UserDto;
import kim.zhyun.serveruser.data.entity.Role;
import kim.zhyun.serveruser.data.entity.User;
import kim.zhyun.serveruser.data.type.RoleType;
import kim.zhyun.serveruser.filter.AuthenticationFilter;
import kim.zhyun.serveruser.repository.RoleRepository;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.SIGNIN_FAIL;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Import(SecurityConfig.class)
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
class MemberServiceImplTest {
    
    @InjectMocks AuthenticationFilter authenticationFilter;
    @InjectMocks SecurityAuthenticationManager authenticationManager;
    @Mock UserRepository userRepository;
    @Mock MemberService userService;
    @Mock PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;
    public MemberServiceImplTest(@Autowired RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    
    @DisplayName("로그인 로직 테스트")
    @Nested
    class LoginTest {
        
        @DisplayName("비회원 접근")
        @Test
        public void anonymous() throws Exception {
            // given
            SignInRequest signInInfo = SignInRequest.of("asdsad@gmail.com", "qwer");
            
            when(userRepository.findByEmail(signInInfo.getEmail())).thenReturn(Optional.empty());
            doThrow(new MemberException(SIGNIN_FAIL)).when(userService).findByEmail(signInInfo.getEmail());
            
            MockHttpServletRequest servletRequest = new MockHttpServletRequest();
            servletRequest.setContentType(APPLICATION_JSON_VALUE);
            servletRequest.setContent(new ObjectMapper().writeValueAsString(signInInfo).getBytes());
            
            
            // when-then
            assertThrows(SIGNIN_FAIL, MemberException.class, () ->
                    authenticationFilter.attemptAuthentication(servletRequest, new MockHttpServletResponse()));
            
            verify(userService, times(1)).findByEmail(signInInfo.getEmail());
        }
        
        @DisplayName("회원 접근 - 비밀번호 틀림")
        @Test
        public void member_password_fail() throws Exception {
            // given
            SignInRequest signInInfo = SignInRequest.of("asdsad@gmail.com", "qwer");
            Role role = roleRepository.findByGrade(RoleType.MEMBER.name());
            User member = User.builder()
                    .id(1L)
                    .email(signInInfo.getEmail())
                    .nickname("nickname")
                    .password(signInInfo.getPassword())
                    .role(role).build();
            
            when(userRepository.findByEmail(signInInfo.getEmail())).thenReturn(Optional.of(member));
            when(userService.findByEmail(signInInfo.getEmail())).thenReturn(UserDto.from(member));
            when(passwordEncoder.matches(signInInfo.getPassword(), member.getPassword())).thenReturn(false);
            
            authenticationFilter.setAuthenticationManager(authenticationManager);
            
            MockHttpServletRequest servletRequest = new MockHttpServletRequest();
            servletRequest.setContentType(APPLICATION_JSON_VALUE);
            servletRequest.setContent(new ObjectMapper().writeValueAsString(signInInfo).getBytes());
            
            
            // when-then
            assertThrows(SIGNIN_FAIL,
                    MemberException.class,
                    () -> authenticationManager.authenticate(
                            authenticationFilter.attemptAuthentication(servletRequest, new MockHttpServletResponse())));
            
            verify(userService, times(2)).findByEmail(signInInfo.getEmail());
        }
        
        @DisplayName("회원 접근 - 로그인 성공")
        @Test
        public void member_password_success() throws Exception {
            // given-when
            SignInRequest signInInfo = SignInRequest.of("asdsad@gmail.com", "qwer");
            Role role = roleRepository.findByGrade(RoleType.MEMBER.name());
            User member = User.builder()
                    .id(1L)
                    .email(signInInfo.getEmail())
                    .nickname("nickname")
                    .password(signInInfo.getPassword())
                    .role(role).build();
            
            when(userRepository.findByEmail(signInInfo.getEmail())).thenReturn(Optional.of(member));
            when(userService.findByEmail(signInInfo.getEmail())).thenReturn(UserDto.from(member));
            when(passwordEncoder.matches(signInInfo.getPassword(), member.getPassword())).thenReturn(true);
            
            authenticationFilter.setAuthenticationManager(authenticationManager);
            
            MockHttpServletRequest servletRequest = new MockHttpServletRequest();
            servletRequest.setContentType(APPLICATION_JSON_VALUE);
            servletRequest.setContent(new ObjectMapper().writeValueAsString(signInInfo).getBytes());
            
            Authentication processing = authenticationFilter.attemptAuthentication(servletRequest, new MockHttpServletResponse());
            when(userService.findByEmail(processing.getName())).thenReturn(UserDto.from(member));
            
            Authentication result = authenticationManager.authenticate(processing);
            
            
            // then
            verify(userService, times(2)).findByEmail(signInInfo.getEmail());
            assertTrue(result.getPrincipal() instanceof JwtUserDto);
        }
    }
    
    
}