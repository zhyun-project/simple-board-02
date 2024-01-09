package kim.zhyun.serveruser.service.impl;

import kim.zhyun.jwt.data.JwtConstants;
import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.jwt.data.JwtUserInfo;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.jwt.repository.JwtUserInfoRepository;
import kim.zhyun.jwt.storage.JwtLogoutStorage;
import kim.zhyun.jwt.util.TimeUnitUtil;
import kim.zhyun.serveruser.advice.MemberException;
import kim.zhyun.serveruser.config.SecurityAuthenticationManager;
import kim.zhyun.serveruser.config.SecurityConfig;
import kim.zhyun.serveruser.controller.SignController;
import kim.zhyun.serveruser.data.SignInRequest;
import kim.zhyun.serveruser.data.UserDto;
import kim.zhyun.serveruser.data.UserGradeUpdateRequest;
import kim.zhyun.serveruser.data.UserUpdateRequest;
import kim.zhyun.serveruser.data.entity.Role;
import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.data.entity.User;
import kim.zhyun.serveruser.data.response.UserResponse;
import kim.zhyun.serveruser.filter.AuthenticationFilter;
import kim.zhyun.serveruser.repository.RoleRepository;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.service.MemberService;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import static kim.zhyun.jwt.data.JwtConstants.JWT_HEADER;
import static kim.zhyun.jwt.data.JwtConstants.JWT_PREFIX;
import static kim.zhyun.jwt.data.JwtResponseMessage.JWT_EXPIRED;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_SIGNIN_FAIL;
import static kim.zhyun.serveruser.data.type.RoleType.*;
import static kim.zhyun.serveruser.util.TestSecurityUser.setAuthentication;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
            
            // when
            assertFalse(jwtLogoutStorage.isLogoutToken(jwt, username));
            controller.logout(JWT_PREFIX + jwt, authentication);
            
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
            
            // when-then
            assertFalse(jwtLogoutStorage.isLogoutToken(jwt, username));
            controller.logout(JWT_PREFIX + jwt, authentication);
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
            
            // when
            assertFalse(jwtLogoutStorage.isLogoutToken(jwt, username));
            controller.logout(JWT_PREFIX + jwt, authentication);
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
    
    @DisplayName("회원 정보 수정 테스트")
    @Nested
    class MemberInfoUpdate {
        
        private final MemberServiceImpl memberService;
        private final UserRepository userRepository;
        private final SessionUserService sessionUserService;
        private final RoleRepository roleRepository;
        public MemberInfoUpdate(@Autowired MemberServiceImpl memberService,
                                         @Autowired UserRepository userRepository,
                                         @Autowired SessionUserService sessionUserService,
                                         @Autowired RoleRepository roleRepository) {
            this.memberService = memberService;
            this.userRepository = userRepository;
            this.sessionUserService = sessionUserService;
            this.roleRepository = roleRepository;
        }
        
        @DisplayName("닉네임 수정 테스트")
        @Nested
        class NicknameTest {
            String username = "member@mem.ber";
            String nickname = "mem1";
            String nicknameChange = "닉넴변경;";
            String password = "1234";
            
            @DisplayName("이전 닉네임과 동일")
            @Test
            public void pass() {
                // given
                User user = mem1();
                UserUpdateRequest request = UserUpdateRequest.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .nickname(user.getNickname()).build();
                
                // when
                MockHttpSession session = new MockHttpSession();
                UserResponse result = memberService.updateUserInfo(session.getId(), request);
                
                // then
                assertThat(UserResponse.from(user)).isEqualTo(result);
            }
            
            
            @DisplayName("닉네임 중복확인 안함")
            @Test
            public void fail_nickname_duplicate_passed() {
                // given
                User user = mem1();
                UserUpdateRequest request = UserUpdateRequest.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .nickname(nicknameChange).build();
                
                // when-then
                MockHttpSession session = new MockHttpSession();
                assertThrows(EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK,
                        MemberException.class,
                        () -> memberService.updateUserInfo(session.getId(), request));
            }
            
            
            @DisplayName("닉네임 수정")
            @Test
            public void success() {
                // given
                User user = mem1();
                UserUpdateRequest request = UserUpdateRequest.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .nickname(nicknameChange).build();
                
                MockHttpSession session = new MockHttpSession();
                
                SessionUser sessionUser = SessionUser.builder()
                        .sessionId(session.getId())
                        .email(user.getEmail())
                        .nickname(nicknameChange).build();
                
                sessionUserService.save(sessionUser);
                
                // when
                UserResponse result = memberService.updateUserInfo(session.getId(), request);
                
                // then
                assertThat(result).isNotEqualTo(UserResponse.from(user));
                assertThat(result.getNickname()).isEqualTo(nicknameChange);
            }
            
            
            @BeforeEach public void init() {
                Role roleMember = roleRepository.findByGrade(TYPE_MEMBER);
                
                User mem1 = User.builder()
                        .email(username)
                        .nickname(nickname)
                        .password(password)
                        .role(roleMember).build();
                
                userRepository.save(mem1);
            }
            @AfterEach public void clean() {
                userRepository.deleteAll();
            }
            
            private User mem1() {
                return userRepository.findByEmail(username).get();
            }
        }
        
        @DisplayName("비밀번호 수정 테스트")
        @Nested
        class PasswordTest {
            String username = "member@mem.ber";
            String nickname = "mem1";
            String password = "1234";
            
            private final PasswordEncoder passwordEncoder;
            public PasswordTest(@Autowired PasswordEncoder passwordEncoder) {
                this.passwordEncoder = passwordEncoder;
            }
            
            @DisplayName("비밀번호 수정")
            @Test
            public void success() {
                // given
                User before = mem1();
                String passwordChange = "passwordChange";
                
                UserUpdateRequest request = UserUpdateRequest.builder()
                        .id(before.getId())
                        .email(before.getEmail())
                        .password(passwordChange).build();
                
                // when
                MockHttpSession session = new MockHttpSession();
                UserResponse afterResponse = memberService.updateUserInfo(session.getId(), request);
                
                // then
                User after = mem1();
                UserResponse beforeResponse = UserResponse.from(before);
                
                assertThat(before).isNotEqualTo(after);
                assertThat(beforeResponse).isEqualTo(afterResponse);
                
                assertThat(before.getPassword()).isNotEqualTo(after.getPassword());
                assertThat(passwordEncoder.matches(passwordChange, after.getPassword())).isTrue();
            }
            
            
            @BeforeEach public void init() {
                Role roleMember = roleRepository.findByGrade(TYPE_MEMBER);
                
                User mem1 = User.builder()
                        .email(username)
                        .nickname(nickname)
                        .password(password)
                        .role(roleMember).build();
                
                userRepository.save(mem1);
            }
            @AfterEach public void clean() {
                userRepository.deleteAll();
            }
            
            public User mem1() {
                return userRepository.findByEmail(username).get();
            }
        }
        
    }
    
    @DisplayName("회원 권한 수정 테스트")
    @Nested
    class MemberGradeUpdateRealTest {
        
        private final MemberServiceImpl memberService;
        private final UserRepository userRepository;
        private final JwtUserInfoRepository jwtUserInfoRepository;
        private final RoleRepository roleRepository;
        public MemberGradeUpdateRealTest(@Autowired MemberServiceImpl memberService,
                                         @Autowired UserRepository userRepository,
                                         @Autowired JwtUserInfoRepository jwtUserInfoRepository,
                                         @Autowired RoleRepository roleRepository) {
            this.memberService = memberService;
            this.userRepository = userRepository;
            this.jwtUserInfoRepository = jwtUserInfoRepository;
            this.roleRepository = roleRepository;
        }
        
        @DisplayName("권한 수정")
        @Test
        public void success() {
            Role roleMember = roleRepository.findByGrade(TYPE_MEMBER);
            
            UserGradeUpdateRequest updateRequest = UserGradeUpdateRequest.builder()
                    .id(1L)
                    .role(TYPE_WITHDRAWAL).build();
            
            User mem1 = User.builder()
                    .id(updateRequest.getId())
                    .email("member@mem.ber")
                    .nickname("mem1")
                    .password("1234")
                    .role(roleMember).build();
            
            userRepository.save(mem1);
            
            // when
            var before  = mem1.getRole().getGrade();
            var updated = memberService.updateUserGrade(updateRequest);
            var after   = updated.getRole().getGrade();
            
            // then
            assertThat(before).isNotEqualTo(after);
            
            JwtUserInfo jwtUserInfo = jwtUserInfoRepository.findById(mem1.getId()).get();
            User user = userRepository.findById(mem1.getId()).get();
            
            assertThat(jwtUserInfo.getId()).isEqualTo(user.getId());
            assertThat(jwtUserInfo.getEmail()).isEqualTo(user.getEmail());
            assertThat(jwtUserInfo.getNickname()).isEqualTo(user.getNickname());
            assertThat(jwtUserInfo.getGrade()).contains(user.getRole().getGrade());
            
            assertThat(jwtUserInfo.getGrade()).doesNotContain(mem1.getRole().getGrade());
        }
        
        @AfterEach public void clean() {
            userRepository.deleteAll();
        }
    }
    
    @DisplayName("회원 탈퇴 테스트")
    @AutoConfigureMockMvc
    @Nested
    class WithdrawalTest {
        private final String ADMIN_USERNAME = "gimwlgus@daum.net";
        private final String WITHDRAWAL_USERNAME = "withdrawal@daum.net";
        
        private final long EXPIRE_TIME;
        private final ChronoUnit EXPIRE_TIME_UNIT;
        
        private RoleRepository roleRepository;
        private UserRepository userRepository;
        private JwtUserInfoRepository jwtUserInfoRepository;
        private PasswordEncoder passwordEncoder;
        private MockMvc mvc;
        
        public WithdrawalTest(@Autowired MockMvc mvc,
                              @Autowired RoleRepository roleRepository,
                              @Autowired UserRepository userRepository,
                              @Autowired JwtUserInfoRepository jwtUserInfoRepository,
                              @Autowired PasswordEncoder passwordEncoder,
                              @Value("${withdrawal.expiration-time}") long expireTime,
                              @Value("${withdrawal.expiration-time-unit}") String expireTimeUnit) {
            this.mvc = mvc;
            this.roleRepository = roleRepository;
            this.userRepository = userRepository;
            this.jwtUserInfoRepository = jwtUserInfoRepository;
            this.passwordEncoder = passwordEncoder;
            this.EXPIRE_TIME = expireTime;
            this.EXPIRE_TIME_UNIT = TimeUnitUtil.timeUnitFrom(expireTimeUnit);
        }
        
        @DisplayName("탈퇴 후 유예기간이 끝나기 전에 관리자에 의해 탈퇴 취소 된 경우 db 데이터 확인 (cron 설정: 5초마다)")
        @Test
        void check_database_re_member() throws Exception {
            // given
            User target = withdrawal().get();
            User admin = admin();
            
            // when
            setAuthentication(admin);
            mvc.perform(put("/user/role")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(UserGradeUpdateRequest.builder()
                                    .id(target.getId())
                                    .role(TYPE_MEMBER).build())))
                    .andExpect(status().isCreated());
            TestSecurityContextHolder.clearContext();
            
            // then
            User targetUpdated = withdrawal().get();
            assertThat(targetUpdated.getRole()).isNotEqualTo(target.getRole());
            assertThat(targetUpdated.isWithdrawal()).isNotEqualTo(target.isWithdrawal());
            assertThat(targetUpdated.getModifiedAt()).isNotEqualTo(target.getModifiedAt());
            
            assertThat(targetUpdated.getId()).isEqualTo(target.getId());
            assertThat(targetUpdated.getEmail()).isEqualTo(target.getEmail());
            assertThat(targetUpdated.getNickname()).isEqualTo(target.getNickname());
            assertThat(targetUpdated.getCreatedAt()).isEqualTo(target.getCreatedAt());
        }
        
        @DisplayName("탈퇴 유예기간이 끝난 후 db 데이터 확인")
        @Test
        void check_database_deleted_user() throws Exception {
            // given
            Optional<User> targetContainer = withdrawal();
            User target = targetContainer.get();
            
            Optional<JwtUserInfo> redisTarget = withdrawalFromRedis(target);
            
            assertThat(targetContainer).isNotEmpty();
            assertThat(redisTarget).isNotEmpty();
            
            // when
            Thread.sleep(Duration.of(EXPIRE_TIME + 11, EXPIRE_TIME_UNIT));
            
            // then
            Optional<User> targetUpdated = withdrawal();
            Optional<JwtUserInfo> redisTargetUpdated = withdrawalFromRedis(target);
            
            assertThat(targetUpdated).isEmpty();
            assertThat(redisTargetUpdated).isEmpty();
        }
        
        @DisplayName("탈퇴한 회원을 관리자가 다시 탈퇴 처리할 경우 회원 db 데이터 확인")
        @Test
        void check_database_re_withdrawal() throws Exception {
            // given
            var admin  = admin();
            var target = withdrawal().get();
            var redisTarget = withdrawalFromRedis(target).get();
            
            // when
            setAuthentication(admin);
            mvc.perform(put("/user/role")
                    .contentType(APPLICATION_JSON_VALUE)
                    .content(new ObjectMapper().writeValueAsString(UserGradeUpdateRequest.builder()
                            .id(target.getId())
                            .role(TYPE_WITHDRAWAL).build())))
                    .andExpect(status().isBadRequest());
            TestSecurityContextHolder.clearContext();
            
            // then
            var targetUpdated = withdrawal().get();
            var redisTargetUpdated = withdrawalFromRedis(target).get();
            
            assertThat(targetUpdated).isEqualTo(target);
            assertThat(redisTargetUpdated).isEqualTo(redisTarget);
        }
        
        
        @BeforeEach public void initUser() {
            Role roleAdmin = roleRepository.findByGrade(TYPE_ADMIN);
            Role roleMember = roleRepository.findByGrade(TYPE_MEMBER);
            Role roleWithdrawal = roleRepository.findByGrade(TYPE_WITHDRAWAL);
            
            String password = passwordEncoder.encode("1234");
            
            userRepository.save(User.builder()
                    .email(ADMIN_USERNAME)
                    .password(password)
                    .nickname("admin")
                    .withdrawal(false)
                    .role(roleAdmin).build());
            
            User savedWithdrawal = userRepository.save(User.builder()
                    .email(WITHDRAWAL_USERNAME)
                    .password(password)
                    .nickname("탈퇴🖐️")
                    .withdrawal(true)
                    .role(roleWithdrawal).build());
            
            jwtUserInfoRepository.save(JwtUserInfo.builder()
                    .id(savedWithdrawal.getId())
                    .email(savedWithdrawal.getEmail())
                    .nickname(savedWithdrawal.getNickname())
                    .grade(savedWithdrawal.getRole().getGrade()).build());
        }
        @AfterEach  public void clean() {
            userRepository.deleteAll();
        }
        
        private User admin() {
            return userRepository.findByEmail(ADMIN_USERNAME).get();
        }
        private Optional<User> withdrawal() {
            return userRepository.findByEmail(WITHDRAWAL_USERNAME);
        }
        private Optional<JwtUserInfo> withdrawalFromRedis(User target) {
            return jwtUserInfoRepository.findById(JwtUserInfo.builder().id(target.getId()).build().getId());
        }
    }
}
