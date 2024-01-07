package kim.zhyun.serveruser.service.impl;

import kim.zhyun.jwt.data.JwtConstants;
import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.jwt.storage.JwtLogoutStorage;
import kim.zhyun.serveruser.advice.MemberException;
import kim.zhyun.serveruser.config.SecurityAuthenticationManager;
import kim.zhyun.serveruser.config.SecurityConfig;
import kim.zhyun.serveruser.controller.SignController;
import kim.zhyun.serveruser.data.SignInRequest;
import kim.zhyun.serveruser.data.UserDto;
import kim.zhyun.serveruser.data.entity.Role;
import kim.zhyun.serveruser.data.entity.User;
import kim.zhyun.serveruser.filter.AuthenticationFilter;
import kim.zhyun.serveruser.repository.RoleRepository;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static kim.zhyun.jwt.data.JwtConstants.JWT_HEADER;
import static kim.zhyun.jwt.data.JwtConstants.JWT_PREFIX;
import static kim.zhyun.jwt.data.JwtResponseMessage.JWT_EXPIRED;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_SIGNIN_FAIL;
import static kim.zhyun.serveruser.data.type.RoleType.TYPE_MEMBER;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import(SecurityConfig.class)
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
class MemberServiceImplTest {
    
    @DisplayName("로그인 로직 테스트")
    @Nested
    class LoginTest {
        
        @InjectMocks AuthenticationFilter authenticationFilter;
        @InjectMocks SecurityAuthenticationManager authenticationManager;
        @Mock UserRepository userRepository;
        @Mock MemberService userService;
        @Mock PasswordEncoder passwordEncoder;
        
        private final RoleRepository roleRepository;
        public LoginTest(@Autowired RoleRepository roleRepository) {
            this.roleRepository = roleRepository;
        }
        
        @DisplayName("비회원 접근")
        @Test
        public void anonymous() throws Exception {
            // given
            SignInRequest signInInfo = SignInRequest.of("asdsad@gmail.com", "qwer");
            
            when(userRepository.findByEmail(signInInfo.getEmail())).thenReturn(Optional.empty());
            doThrow(new MemberException(EXCEPTION_SIGNIN_FAIL)).when(userService).findByEmail(signInInfo.getEmail());
            
            MockHttpServletRequest servletRequest = new MockHttpServletRequest();
            servletRequest.setContentType(APPLICATION_JSON_VALUE);
            servletRequest.setContent(new ObjectMapper().writeValueAsString(signInInfo).getBytes());
            
            
            // when-then
            assertThrows(EXCEPTION_SIGNIN_FAIL, MemberException.class, () ->
                    authenticationFilter.attemptAuthentication(servletRequest, new MockHttpServletResponse()));
            
            verify(userService, times(1)).findByEmail(signInInfo.getEmail());
        }
        
        @DisplayName("회원 접근 - 비밀번호 틀림")
        @Test
        public void member_password_fail() throws Exception {
            // given
            SignInRequest signInInfo = SignInRequest.of("asdsad@gmail.com", "qwer");
            Role role = roleRepository.findByGrade(TYPE_MEMBER);
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
            assertThrows(EXCEPTION_SIGNIN_FAIL,
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
            Role role = roleRepository.findByGrade(TYPE_MEMBER);
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
        
        @AfterEach
        public void clean() {
            userRepository.deleteAll();
        }
    }
    
    @DisplayName("로그아웃 로직 테스트")
    @AutoConfigureMockMvc
    @Nested
    class LogoutTest {
        
        private final JwtConstants jwtItems;
        private final JwtProvider jwtProvider;
        private final JwtLogoutStorage jwtLogoutStorage;
        private final SignController controller;
        private final RedisTemplate<String, String> redisTemplate;
        private final MockMvc mvc;
        public LogoutTest(@Autowired JwtConstants jwtItems,
                          @Autowired JwtProvider jwtProvider,
                          @Autowired JwtLogoutStorage jwtLogoutStorage,
                          @Autowired RedisTemplate<String, String> redisTemplate,
                          @Autowired SignController controller,
                          @Autowired MockMvc mvc) {
            this.jwtItems = jwtItems;
            this.jwtProvider = jwtProvider;
            this.jwtLogoutStorage = jwtLogoutStorage;
            this.redisTemplate = redisTemplate;
            this.controller = controller;
            this.mvc = mvc;
        }
        
        @DisplayName("로그아웃 성공")
        @Test
        public void success() {
            // given
            String username = "gimwlgus@daum.net";
            String nickname = "zhyun";
            String password = "1234";
            
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(JwtUserDto.builder()
                    .id(1L)
                    .email(username)
                    .nickname(nickname).build(), password, Set.of()));
            Authentication authentication = context.getAuthentication();
            
            String jwt = jwtProvider.tokenFrom(authentication);
            
            MockHttpServletRequest servletRequest = new MockHttpServletRequest();
            servletRequest.setContentType(APPLICATION_JSON_VALUE);
            servletRequest.addHeader(JWT_HEADER, JWT_PREFIX + jwt);
            
            // when
            assertFalse(jwtLogoutStorage.isLogoutToken(jwt, username));
            controller.logout(servletRequest, authentication);
            
            // then
            assertTrue(jwtLogoutStorage.isLogoutToken(jwt, username));
        }
        
        @DisplayName("로그아웃 후 토큰 사용 실패")
        @Test
        public void logout_token_health_check() throws Exception {
            // given
            String username = "gimwlgus@daum.net";
            String nickname = "zhyun";
            String password = "1234";
            
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(JwtUserDto.builder()
                    .id(1L)
                    .email(username)
                    .nickname(nickname).build(), password, Set.of()));
            Authentication authentication = context.getAuthentication();
            
            String jwt = jwtProvider.tokenFrom(authentication);
            
            MockHttpServletRequest servletRequest = new MockHttpServletRequest();
            servletRequest.setContentType(APPLICATION_JSON_VALUE);
            servletRequest.addHeader(JWT_HEADER, JWT_PREFIX + jwt);
            
            // when-then
            assertFalse(jwtLogoutStorage.isLogoutToken(jwt, username));
            controller.logout(servletRequest, authentication);
            assertTrue(jwtLogoutStorage.isLogoutToken(jwt, username));
            
            mvc.perform(get("/user").header(JWT_HEADER, JWT_PREFIX + jwt))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(JWT_EXPIRED));
        }
        
        @DisplayName("로그아웃 후 토큰 만료 시간 끝난 후에 redis에 남아있는지 확인")
        @Test
        public void logout_token_expired() throws Exception {
            // given
            String username = "gimwlgus@daum.net";
            String nickname = "zhyun";
            String password = "1234";
            
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(JwtUserDto.builder()
                    .id(1L)
                    .email(username)
                    .nickname(nickname).build(), password, Set.of()));
            Authentication authentication = context.getAuthentication();
            
            String jwt = jwtProvider.tokenFrom(authentication);
            
            MockHttpServletRequest servletRequest = new MockHttpServletRequest();
            servletRequest.setContentType(APPLICATION_JSON_VALUE);
            servletRequest.addHeader(JWT_HEADER, JWT_PREFIX + jwt);
            
            // when
            assertFalse(jwtLogoutStorage.isLogoutToken(jwt, username));
            controller.logout(servletRequest, authentication);
            assertTrue(jwtLogoutStorage.isLogoutToken(jwt, username));
            
            Thread.sleep(Duration.of(jwtItems.expiredTime, jwtItems.expiredTimeUnit.toChronoUnit()));
            
            // then
            assertFalse(jwtLogoutStorage.isLogoutToken(jwt, username));
        }
        
        @BeforeEach
        public void clean() {
            redisTemplate.keys("*").forEach(redisTemplate::delete);
        }
    }
}
