package kim.zhyun.serveruser.service.impl;

import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.jwt.domain.repository.JwtUserInfoRepository;
import kim.zhyun.jwt.domain.service.JwtLogoutService;
import kim.zhyun.jwt.util.TimeUnitUtil;
import kim.zhyun.serveruser.common.advice.MemberException;
import kim.zhyun.serveruser.config.SecurityAuthenticationManager;
import kim.zhyun.serveruser.config.SecurityConfig;
import kim.zhyun.serveruser.domain.member.repository.UserEntity;
import kim.zhyun.serveruser.domain.member.business.MemberBusiness;
import kim.zhyun.serveruser.domain.member.controller.MemberApiController;
import kim.zhyun.serveruser.filter.model.SignInRequest;
import kim.zhyun.serveruser.domain.member.controller.model.UserGradeUpdateRequest;
import kim.zhyun.serveruser.domain.member.controller.model.UserUpdateRequest;
import kim.zhyun.serveruser.domain.signup.repository.Role;
import kim.zhyun.serveruser.domain.signup.repository.SessionUser;
import kim.zhyun.serveruser.domain.member.controller.model.UserResponse;
import kim.zhyun.serveruser.domain.member.converter.UserConverter;
import kim.zhyun.serveruser.filter.AuthenticationFilter;
import kim.zhyun.serveruser.domain.signup.repository.RoleRepository;
import kim.zhyun.serveruser.domain.member.repository.UserRepository;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.domain.member.service.MemberService;
import kim.zhyun.serveruser.domain.member.service.SessionUserService;
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

import static kim.zhyun.jwt.constants.JwtConstants.JWT_HEADER;
import static kim.zhyun.jwt.constants.JwtConstants.JWT_PREFIX;
import static kim.zhyun.jwt.constants.JwtExceptionMessageConstants.JWT_EXPIRED;
import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK;
import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_SIGNIN_FAIL;
import static kim.zhyun.serveruser.common.model.type.RoleType.*;
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
        @Mock JwtProvider jwtProvider;
        @Mock UserRepository userRepository;
        @Mock MemberService userService;
        @Mock PasswordEncoder passwordEncoder;
        
        private final RoleRepository roleRepository;
        private final UserConverter userConverter;
        public LoginTest(@Autowired RoleRepository roleRepository,
                         @Autowired UserConverter userConverter) {
            this.roleRepository = roleRepository;
            this.userConverter = userConverter;
        }
        
        @DisplayName("비회원 접근")
        @Test
        public void anonymous() throws Exception {
            // given
            SignInRequest signInInfo = SignInRequest.of("asdsad@gmail.com", "qwer");
            
            when(userRepository.findByEmail(signInInfo.getEmail())).thenReturn(Optional.empty());
            doThrow(new MemberException(EXCEPTION_SIGNIN_FAIL)).when(userService).findByEmailWithThrow(signInInfo.getEmail());
            
            MockHttpServletRequest servletRequest = new MockHttpServletRequest();
            servletRequest.setContentType(APPLICATION_JSON_VALUE);
            servletRequest.setContent(new ObjectMapper().writeValueAsString(signInInfo).getBytes());
            
            authenticationFilter.setAuthenticationManager(authenticationManager);
            
            // when-then
            assertThrows(EXCEPTION_SIGNIN_FAIL, MemberException.class, () ->
                    authenticationFilter.attemptAuthentication(servletRequest, new MockHttpServletResponse()));
            
            verify(userService, times(1)).findByEmailWithThrow(signInInfo.getEmail());
        }
        
        @DisplayName("회원 접근 - 비밀번호 틀림")
        @Test
        public void member_password_fail() throws Exception {
            // given
            SignInRequest signInInfo = SignInRequest.of("asdsad@gmail.com", "qwer");
            Role role = roleRepository.findByGrade(TYPE_MEMBER);
            UserEntity member = UserEntity.builder()
                    .id(1L)
                    .email(signInInfo.getEmail())
                    .nickname("nickname")
                    .password(signInInfo.getPassword())
                    .role(role).build();
            
            when(userRepository.findByEmail(signInInfo.getEmail())).thenReturn(Optional.of(member));
            when(userService.findByEmailWithThrow(signInInfo.getEmail())).thenReturn(member);
            when(passwordEncoder.matches(signInInfo.getPassword(), member.getPassword())).thenReturn(false);
            
            MockHttpServletRequest servletRequest = new MockHttpServletRequest();
            servletRequest.setContentType(APPLICATION_JSON_VALUE);
            servletRequest.setContent(new ObjectMapper().writeValueAsString(signInInfo).getBytes());
            
            authenticationFilter.setAuthenticationManager(authenticationManager);
            
            // when-then
            assertThrows(EXCEPTION_SIGNIN_FAIL,
                    MemberException.class,
                    () -> authenticationManager.authenticate(
                            authenticationFilter.attemptAuthentication(servletRequest, new MockHttpServletResponse())));
            
            verify(userService, times(1)).findByEmailWithThrow(signInInfo.getEmail());
        }
        
        @DisplayName("회원 접근 - 로그인 성공")
        @Test
        public void member_password_success() throws Exception {
            // given-when
            SignInRequest signInInfo = SignInRequest.of("asdsad@gmail.com", "qwer");
            Role role = roleRepository.findByGrade(TYPE_MEMBER);
            UserEntity member = UserEntity.builder()
                    .id(1L)
                    .email(signInInfo.getEmail())
                    .nickname("nickname")
                    .password(signInInfo.getPassword())
                    .role(role).build();
            
            when(userRepository.findByEmail(signInInfo.getEmail())).thenReturn(Optional.of(member));
            when(userService.findByEmailWithThrow(signInInfo.getEmail())).thenReturn(member);
            when(passwordEncoder.matches(signInInfo.getPassword(), member.getPassword())).thenReturn(true);
            
            authenticationFilter.setAuthenticationManager(authenticationManager);
            
            MockHttpServletRequest servletRequest = new MockHttpServletRequest();
            servletRequest.setContentType(APPLICATION_JSON_VALUE);
            servletRequest.setContent(new ObjectMapper().writeValueAsString(signInInfo).getBytes());
            
            Authentication processing = authenticationFilter.attemptAuthentication(servletRequest, new MockHttpServletResponse());
            when(userService.findByEmailWithThrow(processing.getName())).thenReturn(member);
            
            Authentication result = authenticationManager.authenticate(processing);
            
            
            // then
            verify(userService, times(1)).findByEmailWithThrow(signInInfo.getEmail());
            assertTrue(result.getPrincipal() instanceof JwtUserInfoDto);
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
        
        private final JwtProvider jwtProvider;
        private final JwtLogoutService jwtLogoutService;
        private final MemberApiController controller;
        private final RedisTemplate<String, String> redisTemplate;
        private final MockMvc mvc;
        private final Long expiredTime;
        private final String expiredTimeUnit;
        public LogoutTest(@Autowired JwtProvider jwtProvider,
                          @Autowired JwtLogoutService jwtLogoutService,
                          @Autowired RedisTemplate<String, String> redisTemplate,
                          @Autowired MemberApiController controller,
                          @Autowired MockMvc mvc,
                          @Value("${token.expiration-time}") Long expiredTime,
                          @Value("${token.expiration-time-unit}") String expiredTimeUnit) {
            this.jwtProvider = jwtProvider;
            this.jwtLogoutService = jwtLogoutService;
            this.redisTemplate = redisTemplate;
            this.controller = controller;
            this.mvc = mvc;
            this.expiredTime = expiredTime;
            this.expiredTimeUnit = expiredTimeUnit;
        }
        
        @DisplayName("로그아웃 성공")
        @Test
        public void success() {
            // given
            String username = "gimwlgus@daum.net";
            String nickname = "zhyun";
            String password = "1234";
            
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(JwtUserInfoDto.builder()
                    .id(1L)
                    .email(username)
                    .nickname(nickname).build(), password, Set.of()));
            Authentication authentication = context.getAuthentication();
            
            jwtProvider.setJwtExpired(expiredTime, expiredTimeUnit);
            String jwt = jwtProvider.tokenFrom(authentication);
            
            // when
            assertFalse(jwtLogoutService.isLogoutToken(jwt, username));
            controller.logout(JWT_PREFIX + jwt);
            
            // then
            assertTrue(jwtLogoutService.isLogoutToken(jwt, username));
        }
        
        @DisplayName("로그아웃 후 토큰 사용 실패")
        @Test
        public void logout_token_health_check() throws Exception {
            // given
            String username = "gimwlgus@daum.net";
            String nickname = "zhyun";
            String password = "1234";
            
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(JwtUserInfoDto.builder()
                    .id(1L)
                    .email(username)
                    .nickname(nickname).build(), password, Set.of()));
            Authentication authentication = context.getAuthentication();
            
            jwtProvider.setJwtExpired(expiredTime, expiredTimeUnit);
            String jwt = jwtProvider.tokenFrom(authentication);
            
            // when-then
            assertFalse(jwtLogoutService.isLogoutToken(jwt, username));
            controller.logout(JWT_PREFIX + jwt);
            assertTrue(jwtLogoutService.isLogoutToken(jwt, username));
            
            mvc.perform(get("/all").header(JWT_HEADER, JWT_PREFIX + jwt))
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
            context.setAuthentication(new UsernamePasswordAuthenticationToken(JwtUserInfoDto.builder()
                    .id(1L)
                    .email(username)
                    .nickname(nickname).build(), password, Set.of()));
            Authentication authentication = context.getAuthentication();
            
            jwtProvider.setJwtExpired(expiredTime, expiredTimeUnit);
            String jwt = jwtProvider.tokenFrom(authentication);
            
            // when
            assertFalse(jwtLogoutService.isLogoutToken(jwt, username));
            controller.logout(JWT_PREFIX + jwt);
            assertTrue(jwtLogoutService.isLogoutToken(jwt, username));
            
            Thread.sleep(Duration.of(jwtProvider.expiredTime, jwtProvider.expiredTimeUnit.toChronoUnit()));
            
            // then
            assertFalse(jwtLogoutService.isLogoutToken(jwt, username));
        }
        
        @BeforeEach
        public void clean() {
            redisTemplate.keys("*").forEach(redisTemplate::delete);
        }
    }
    
    @DisplayName("회원 정보 수정 테스트")
    @Nested
    class MemberInfoUpdate {
        
        private final MemberBusiness memberBusiness;
        private final UserRepository userRepository;
        private final SessionUserService sessionUserService;
        private final RoleRepository roleRepository;
        private final UserConverter userConverter;
        public MemberInfoUpdate(@Autowired MemberBusiness memberBusiness,
                                @Autowired UserRepository userRepository,
                                @Autowired SessionUserService sessionUserService,
                                @Autowired RoleRepository roleRepository,
                                @Autowired UserConverter userConverter) {
            this.memberBusiness = memberBusiness;
            this.userRepository = userRepository;
            this.sessionUserService = sessionUserService;
            this.roleRepository = roleRepository;
            this.userConverter = userConverter;
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
                UserEntity userEntity = mem1();
                UserUpdateRequest request = UserUpdateRequest.builder()
                        .id(userEntity.getId())
                        .email(userEntity.getEmail())
                        .nickname(userEntity.getNickname()).build();
                
                // when
                MockHttpSession session = new MockHttpSession();
                UserResponse result = memberBusiness.updateUserInfo(session.getId(), request);
                
                // then
                assertThat(userConverter.toResponse(userEntity)).isEqualTo(result);
            }
            
            
            @DisplayName("닉네임 중복확인 안함")
            @Test
            public void fail_nickname_duplicate_passed() {
                // given
                UserEntity userEntity = mem1();
                UserUpdateRequest request = UserUpdateRequest.builder()
                        .id(userEntity.getId())
                        .email(userEntity.getEmail())
                        .nickname(nicknameChange).build();
                
                // when-then
                MockHttpSession session = new MockHttpSession();
                assertThrows(EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK,
                        MemberException.class,
                        () -> memberBusiness.updateUserInfo(session.getId(), request));
            }
            
            
            @DisplayName("닉네임 수정")
            @Test
            public void success() {
                // given
                UserEntity userEntity = mem1();
                UserUpdateRequest request = UserUpdateRequest.builder()
                        .id(userEntity.getId())
                        .email(userEntity.getEmail())
                        .nickname(nicknameChange).build();
                
                MockHttpSession session = new MockHttpSession();
                
                SessionUser sessionUser = SessionUser.builder()
                        .sessionId(session.getId())
                        .email(userEntity.getEmail())
                        .nickname(nicknameChange).build();
                
                sessionUserService.save(sessionUser);
                
                // when
                UserResponse result = memberBusiness.updateUserInfo(session.getId(), request);
                
                // then
                assertThat(result).isNotEqualTo(userConverter.toResponse(userEntity));
                assertThat(result.getNickname()).isEqualTo(nicknameChange);
            }
            
            
            @BeforeEach public void init() {
                Role roleMember = roleRepository.findByGrade(TYPE_MEMBER);
                
                UserEntity mem1 = UserEntity.builder()
                        .email(username)
                        .nickname(nickname)
                        .password(password)
                        .role(roleMember).build();
                
                userRepository.save(mem1);
            }
            @AfterEach public void clean() {
                userRepository.deleteAll();
            }
            
            private UserEntity mem1() {
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
                UserEntity before = mem1();
                String passwordChange = "passwordChange";
                
                UserUpdateRequest request = UserUpdateRequest.builder()
                        .id(before.getId())
                        .email(before.getEmail())
                        .password(passwordChange).build();
                
                // when
                MockHttpSession session = new MockHttpSession();
                UserResponse afterResponse = memberBusiness.updateUserInfo(session.getId(), request);
                
                // then
                UserEntity after = mem1();
                UserResponse beforeResponse = userConverter.toResponse(before);
                
                assertThat(before).isNotEqualTo(after);
                assertThat(beforeResponse).isEqualTo(afterResponse);
                
                assertThat(before.getPassword()).isNotEqualTo(after.getPassword());
                assertThat(passwordEncoder.matches(passwordChange, after.getPassword())).isTrue();
            }
            
            
            @BeforeEach public void init() {
                Role roleMember = roleRepository.findByGrade(TYPE_MEMBER);
                
                UserEntity mem1 = UserEntity.builder()
                        .email(username)
                        .nickname(nickname)
                        .password(password)
                        .role(roleMember).build();
                
                userRepository.save(mem1);
            }
            @AfterEach public void clean() {
                userRepository.deleteAll();
            }
            
            public UserEntity mem1() {
                return userRepository.findByEmail(username).get();
            }
        }
        
    }
    
    @DisplayName("회원 권한 수정 테스트")
    @Nested
    class MemberGradeUpdateRealTest {
        
        private final MemberService memberService;
        private final UserRepository userRepository;
        private final JwtUserInfoRepository jwtUserInfoRepository;
        private final RoleRepository roleRepository;
        public MemberGradeUpdateRealTest(@Autowired MemberService memberService,
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
            
            UserEntity mem1 = UserEntity.builder()
                    .email("member@mem.ber")
                    .nickname("mem1")
                    .password("1234")
                    .role(roleMember).build();
            userRepository.save(mem1);
            
            UserGradeUpdateRequest updateRequest = UserGradeUpdateRequest.builder()
                    .id(userRepository.findByEmail(mem1.getEmail()).get().getId())
                    .role(TYPE_WITHDRAWAL).build();
            
            // when
            var before  = mem1.getRole().getGrade();
            var updated = memberService.updateUserGrade(updateRequest);
            var after   = updated.getRole().getGrade();
            
            // then
            assertThat(before).isNotEqualTo(after);
            
            JwtUserInfoEntity jwtUserInfoEntity = jwtUserInfoRepository.findById(mem1.getId()).get();
            UserEntity userEntity = userRepository.findById(mem1.getId()).get();
            
            assertThat(jwtUserInfoEntity.getId()).isEqualTo(userEntity.getId());
            assertThat(jwtUserInfoEntity.getEmail()).isEqualTo(userEntity.getEmail());
            assertThat(jwtUserInfoEntity.getNickname()).isEqualTo(userEntity.getNickname());
            assertThat(jwtUserInfoEntity.getGrade()).contains(userEntity.getRole().getGrade());
            
            assertThat(jwtUserInfoEntity.getGrade()).doesNotContain(mem1.getRole().getGrade());
        }
        
        @AfterEach public void clean() {
            userRepository.deleteAll();
            jwtUserInfoRepository.deleteAll();
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
            UserEntity target = withdrawal().get();
            UserEntity admin = admin();
            
            // when
            setAuthentication(admin);
            mvc.perform(put("/role")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(UserGradeUpdateRequest.builder()
                                    .id(target.getId())
                                    .role(TYPE_MEMBER).build())))
                    .andExpect(status().isCreated());
            TestSecurityContextHolder.clearContext();
            
            // then
            UserEntity targetUpdated = withdrawal().get();
            assertThat(targetUpdated.getRole()).isNotEqualTo(target.getRole());
            assertThat(targetUpdated.isWithdrawal()).isNotEqualTo(target.isWithdrawal());
            assertThat(targetUpdated.getModifiedAt()).isNotEqualTo(target.getModifiedAt());
            
            assertThat(targetUpdated.getId()).isEqualTo(target.getId());
            assertThat(targetUpdated.getEmail()).isEqualTo(target.getEmail());
            assertThat(targetUpdated.getNickname()).isEqualTo(target.getNickname());
            assertThat(targetUpdated.getCreatedAt()).isEqualTo(target.getCreatedAt());
        }
        
        @Disabled("article server 실행되어있어야 통과")
        @DisplayName("탈퇴 유예기간이 끝난 후 db 데이터 확인")
        @Test
        void check_database_deleted_user() throws Exception {
            // given
            Optional<UserEntity> targetContainer = withdrawal();
            UserEntity target = targetContainer.get();
            
            Optional<JwtUserInfoEntity> redisTarget = withdrawalFromRedis(target);
            
            assertThat(targetContainer).isNotEmpty();
            assertThat(redisTarget).isNotEmpty();
            
            // when
            Thread.sleep(Duration.of(EXPIRE_TIME + 11, EXPIRE_TIME_UNIT));
            
            // then
            Optional<UserEntity> targetUpdated = withdrawal();
            Optional<JwtUserInfoEntity> redisTargetUpdated = withdrawalFromRedis(target);
            
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
            mvc.perform(put("/role")
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
            
            userRepository.save(UserEntity.builder()
                    .email(ADMIN_USERNAME)
                    .password(password)
                    .nickname("admin")
                    .withdrawal(false)
                    .role(roleAdmin).build());
            
            UserEntity savedWithdrawal = userRepository.save(UserEntity.builder()
                    .email(WITHDRAWAL_USERNAME)
                    .password(password)
                    .nickname("탈퇴🖐️")
                    .withdrawal(true)
                    .role(roleWithdrawal).build());
            
            jwtUserInfoRepository.save(JwtUserInfoEntity.builder()
                    .id(savedWithdrawal.getId())
                    .email(savedWithdrawal.getEmail())
                    .nickname(savedWithdrawal.getNickname())
                    .grade(savedWithdrawal.getRole().getGrade()).build());
        }
        @AfterEach  public void clean() {
            userRepository.deleteAll();
            jwtUserInfoRepository.deleteAll();
        }
        
        private UserEntity admin() {
            return userRepository.findByEmail(ADMIN_USERNAME).get();
        }
        private Optional<UserEntity> withdrawal() {
            return userRepository.findByEmail(WITHDRAWAL_USERNAME);
        }
        private Optional<JwtUserInfoEntity> withdrawalFromRedis(UserEntity target) {
            return jwtUserInfoRepository.findById(JwtUserInfoEntity.builder().id(target.getId()).build().getId());
        }
    }
}
