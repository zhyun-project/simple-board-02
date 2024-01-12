package kim.zhyun.serveruser.controller;

import kim.zhyun.jwt.data.JwtUserInfo;
import kim.zhyun.jwt.repository.JwtUserInfoRepository;
import kim.zhyun.jwt.util.TimeUnitUtil;
import kim.zhyun.serveruser.config.SchedulerConfig;
import kim.zhyun.serveruser.config.SecurityConfig;
import kim.zhyun.serveruser.data.NicknameDto;
import kim.zhyun.serveruser.data.SignInRequest;
import kim.zhyun.serveruser.data.UserGradeUpdateRequest;
import kim.zhyun.serveruser.data.UserUpdateRequest;
import kim.zhyun.serveruser.data.entity.Role;
import kim.zhyun.serveruser.data.entity.User;
import kim.zhyun.serveruser.repository.RoleRepository;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.service.NicknameReserveService;
import kim.zhyun.serveruser.utils.DateTimeUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static kim.zhyun.jwt.data.JwtConstants.JWT_HEADER;
import static kim.zhyun.jwt.data.JwtConstants.JWT_PREFIX;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.*;
import static kim.zhyun.serveruser.data.message.ResponseMessage.*;
import static kim.zhyun.serveruser.data.type.RoleType.*;
import static kim.zhyun.serveruser.util.TestSecurityUser.setAuthentication;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("member controller test")
@ExtendWith(RedisTestContainer.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@SpringBootTest
class MemberControllerTest {
    private final String ADMIN_USERNAME = "gimwlgus@daum.net";
    private final String MEMBER_1_USERNAME = "member1@daum.net";
    private final String MEMBER_2_USERNAME = "member2@daum.net";
    private final String WITHDRAWAL_USERNAME = "withdrawal@daum.net";
    
    private final MockMvc mvc;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NicknameReserveService nicknameReserveService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUserInfoRepository jwtUserInfoRepository;
    public MemberControllerTest(@Autowired MockMvc mvc,
                                @Autowired UserRepository userRepository,
                                @Autowired RoleRepository roleRepository,
                                @Autowired NicknameReserveService nicknameReserveService,
                                @Autowired PasswordEncoder passwordEncoder,
                                @Autowired JwtUserInfoRepository jwtUserInfoRepository) {
        this.mvc = mvc;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.nicknameReserveService = nicknameReserveService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUserInfoRepository = jwtUserInfoRepository;
    }
    
    @DisplayName("회원 계정 조회 테스트")
    @Nested
    class ShowMemberInfo {
        
        @DisplayName("전체 계정 조회 - 권한이 `WITHDRAWAL`인 유저의 접근")
        @Test
        @WithMockUser(roles = TYPE_WITHDRAWAL)
        public void find_by_all_from_withdrawal() throws Exception {
            mvc.perform(get("/user"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @DisplayName("전체 계정 조회 - 권한이 `MEMBER`인 유저의 접근")
        @Test
        @WithMockUser(roles = TYPE_MEMBER)
        public void find_by_all_from_member() throws Exception {
            mvc.perform(get("/user"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @DisplayName("전체 계정 조회 - 권한이 `ADMIN`인 유저의 접근")
        @Test
        @WithMockUser(roles = TYPE_ADMIN)
        public void find_by_all_from_admin() throws Exception {
            mvc.perform(get("/user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_USER_REFERENCE_ALL))
                    .andDo(print());
        }
        
        @DisplayName("내 계정 조회 - 실패 : 탈퇴자")
        @Test
        public void fail_find_by_id_from_withdrawal() throws Exception {
            User me = withdrawal();
            setAuthentication(me);
            
            mvc.perform(get("/user/{id}", me.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @DisplayName("내 계정 조회 - 성공")
        @Test
        public void find_by_id_from_me() throws Exception {
            User me = member_1();
            setAuthentication(me);
            
            mvc.perform(get("/user/{id}", me.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_USER_REFERENCE_ME))
                    .andDo(print());
        }
        
        @DisplayName("다른 사람 계정 조회 - 실패")
        @Test
        @WithMockUser(roles = TYPE_MEMBER)
        public void find_by_id_from_others() throws Exception {
            User me = member_1();
            User other = member_2();
            setAuthentication(me);
            
            mvc.perform(get("/user/{id}", other.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
    }
    
    
    @DisplayName("회원 계정 수정 테스트")
    @Nested
    class UpdateMemberInfo {
        
        @DisplayName("실패 - 다른 사람 정보 수정 시도")
        @Test
        public void fail_other_info_update() throws Exception {
            // given
            User me = member_1();
            User other = member_2();
            setAuthentication(me);
            
            // when-then
            mvc.perform(put("/user/{}", other.getId())
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(UserUpdateRequest.builder()
                                    .id(other.getId())
                                    .email(other.getEmail())
                                    .password("왔다갑니다").build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @DisplayName("실패 - 탈퇴자")
        @Test
        public void fail_withdrawal_info_update() throws Exception {
            // given
            User me = withdrawal();
            setAuthentication(me);
            
            // when-then
            mvc.perform(put("/user/{}", me.getId())
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(UserUpdateRequest.builder()
                                    .id(me.getId())
                                    .email(me.getEmail())
                                    .password("비밀번호").build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
        
        @Nested
        class NicknameChange {
            
            @DisplayName("닉네임 변경 실패 - 포맷 미준수")
            @Test
            public void fail_my_nickname_format_non_compliance() throws Exception {
                // given
                User me = member_1();
                setAuthentication(me);
                
                // when-then
                mvc.perform(put("/user/{}", me.getId())
                                .contentType(APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(UserUpdateRequest.builder()
                                        .id(me.getId())
                                        .email(me.getEmail())
                                        .nickname("1234567").build())))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(false))
                        .andExpect(jsonPath("$.message").value(EXCEPTION_VALID_FORMAT))
                        .andExpect(jsonPath("$.result.[0].field").value("nickname"))
                        .andExpect(jsonPath("$.result.[0].message").value(EXCEPTION_VALID_NICKNAME_FORMAT))
                        .andDo(print());
            }
            
            @DisplayName("닉네임 변경 실패 - 중복확인 안함")
            @Test
            public void fail_my_nickname_duplicate_check_passed() throws Exception {
                // given
                User me = member_1();
                setAuthentication(me);
                
                // when-then
                mvc.perform(put("/user/{}", me.getId())
                                .contentType(APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(UserUpdateRequest.builder()
                                        .id(me.getId())
                                        .email(me.getEmail())
                                        .nickname("닉네임").build())))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(false))
                        .andExpect(jsonPath("$.message").value(EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK))
                        .andDo(print());
            }
            
            @DisplayName("닉네임 변경 - 통과 : 같은 닉네임")
            @Test
            public void passed_my_nickname_change() throws Exception {
                // given
                User me = member_1();
                setAuthentication(me);
                
                // when-then
                mvc.perform(put("/user/{}", me.getId())
                                .contentType(APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(UserUpdateRequest.builder()
                                        .id(me.getId())
                                        .email(me.getEmail())
                                        .nickname(me.getNickname()).build())))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status").value(true))
                        .andExpect(jsonPath("$.message").value(String.format(
                                RESPONSE_USER_INFO_UPDATE, me.getNickname())))
                        .andDo(print());
            }
            
            @DisplayName("닉네임 변경 - 통과")
            @Test
            public void success_my_nickname_change() throws Exception {
                // given
                User me = member_1();
                setAuthentication(me);
                
                String newNickname = "얼거스";
                
                MockHttpSession session = new MockHttpSession();
                nicknameReserveService.saveNickname(NicknameDto.builder()
                        .sessionId(session.getId())
                        .nickname(newNickname).build());
                
                // when-then
                mvc.perform(put("/user/{}", me.getId())
                                .session(session)
                                .contentType(APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(UserUpdateRequest.builder()
                                        .id(me.getId())
                                        .email(me.getEmail())
                                        .nickname(newNickname).build())))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status").value(true))
                        .andExpect(jsonPath("$.message").value(String.format(
                                RESPONSE_USER_INFO_UPDATE, newNickname)))
                        .andDo(print());
            }
        }
        
        @Nested
        class PasswordChange {

            @DisplayName("실패 - 비밀번호 포맷 미준수 1")
            @Test
            public void fail1() throws Exception {
                // given
                User me = member_1();
                setAuthentication(me);
                
                // when-then
                mvc.perform(put("/user/{}", me.getId())
                                .contentType(APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(UserUpdateRequest.builder()
                                        .id(me.getId())
                                        .email(me.getEmail())
                                        .password("123").build())))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(false))
                        .andExpect(jsonPath("$.message").value(EXCEPTION_VALID_FORMAT))
                        .andExpect(jsonPath("$.result.[0].field").value("password"))
                        .andExpect(jsonPath("$.result.[0].message").value(EXCEPTION_VALID_PASSWORD_FORMAT))
                        .andDo(print());
            }
            
            @DisplayName("실패 - 비밀번호 포맷 미준수 2")
            @Test
            public void fail2() throws Exception {
                // given
                User me = member_1();
                setAuthentication(me);
                
                // when-then
                mvc.perform(put("/user/{}", me.getId())
                                .contentType(APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(UserUpdateRequest.builder()
                                        .id(me.getId())
                                        .email(me.getEmail())
                                        .password("").build())))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(false))
                        .andExpect(jsonPath("$.message").value(EXCEPTION_VALID_FORMAT))
                        .andExpect(jsonPath("$.result.[0].field").value("password"))
                        .andExpect(jsonPath("$.result.[0].message").value(EXCEPTION_VALID_PASSWORD_FORMAT))
                        .andDo(print());
            }
            
            @DisplayName("성공")
            @Test
            public void success1() throws Exception {
                // given
                User me = member_1();
                setAuthentication(me);
                
                // when-then
                mvc.perform(put("/user/{}", me.getId())
                                .contentType(APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(UserUpdateRequest.builder()
                                        .id(me.getId())
                                        .email(me.getEmail())
                                        .password("1234").build())))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status").value(true))
                        .andExpect(jsonPath("$.message").value(String.format(
                                RESPONSE_USER_INFO_UPDATE, me.getNickname())))
                        .andDo(print());
            }
            
            @DisplayName("성공 - 변경한 비밀번호로 db password 조회")
            @Test
            public void success2() throws Exception {
                // given
                User me = member_1();
                setAuthentication(me);
                
                String newPassword = "변경하고갑니다";
                
                // when-then
                mvc.perform(put("/user/{}", me.getId())
                                .contentType(APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(UserUpdateRequest.builder()
                                        .id(me.getId())
                                        .email(me.getEmail())
                                        .password(newPassword).build())))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status").value(true))
                        .andExpect(jsonPath("$.message").value(String.format(
                                RESPONSE_USER_INFO_UPDATE, me.getNickname())))
                        .andDo(print());
                
                User updatedMe = member_1();
                assertTrue(passwordEncoder.matches(newPassword, updatedMe.getPassword()));
            }
        }
    }
    
    
    @DisplayName("회원 권한 수정 테스트")
    @Nested
    class UpdateMemberGrade {
        
        @DisplayName("실패 - `MEMBER`의 접근")
        @Test
        public void fail_member_access() throws Exception {
            // given
            User member = member_1();
            User target = member_2();
            
            setAuthentication(member);
            
            // when-then
            mvc.perform(put("/user/role")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(UserGradeUpdateRequest.builder()
                                    .id(target.getId())
                                    .role(TYPE_WITHDRAWAL).build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
            
            assertEquals(TYPE_MEMBER, member_2().getRole().getGrade());
        }
        
        @DisplayName("실패 - `WITHDRAWAL`의 접근")
        @Test
        public void fail_withdrawal_access() throws Exception {
            // given
            User withdrawal = withdrawal();
            User target = member_2();
            
            setAuthentication(withdrawal);
            
            // when-then
            mvc.perform(put("/user/role")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(UserGradeUpdateRequest.builder()
                                    .id(target.getId())
                                    .role(TYPE_WITHDRAWAL).build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
            
            assertEquals(TYPE_MEMBER, member_2().getRole().getGrade());
        }
        
        @DisplayName("실패 - 권한 입력 안됨")
        @Test
        public void fail_grade_setting_is_null() throws Exception {
            // given
            User admin = admin();
            User target = member_2();
            
            setAuthentication(admin);
            
            // when-then
            mvc.perform(put("/user/role")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(UserGradeUpdateRequest.builder()
                                    .id(target.getId()).build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_VALID_FORMAT))
                    .andDo(print());
        }
        
        @DisplayName("성공")
        @Test
        public void success() throws Exception {
            // given
            User admin = admin();
            User target = member_2();
            
            setAuthentication(admin);
            
            // when-then
            String updateRoleType = TYPE_WITHDRAWAL;
            
            assertNotEquals(target.getRole().getGrade(), updateRoleType);
            
            mvc.perform(put("/user/role")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(UserGradeUpdateRequest.builder()
                                    .id(target.getId())
                                    .role(updateRoleType).build())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(String.format(
                            RESPONSE_USER_GRADE_UPDATE, target.getNickname(), updateRoleType)))
                    .andDo(print());
            
            target = member_2();
            assertEquals(target.getRole().getGrade(), updateRoleType);
        }
    }
    
    @DisplayName("회원 탈퇴 테스트")
    @Nested
    class WithdrawalTest {
        
        long EXPIRE_TIME;
        ChronoUnit EXPIRE_TIME_UNIT;
        
        public WithdrawalTest(@Value("${withdrawal.expiration-time}") long expireTime,
                              @Value("${withdrawal.expiration-time-unit}") String expireTimeUnit) {
            this.EXPIRE_TIME = expireTime;
            this.EXPIRE_TIME_UNIT = TimeUnitUtil.timeUnitFrom(expireTimeUnit);
        }
        
        @DisplayName("성공")
        @Test
        public void success() throws Exception {
            // given
            User target = member_1();
            setAuthentication(target);
            
            SignInRequest signInRequest = SignInRequest.of(target.getEmail(), "1234");
            String jwt = mvc.perform(post("/login")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(signInRequest)))
                    .andReturn()
                    .getResponse().getHeader(JWT_HEADER);
            
            // when-then
            mvc.perform(delete("/withdrawal")
                            .header(JWT_HEADER, JWT_PREFIX + jwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(String.format(RESPONSE_USER_WITHDRAWAL, target.getNickname(), target.getEmail())))
                    .andDo(print());
        }
        
        @DisplayName("탈퇴 후 관리자가 탈퇴 철회한 경우 로그인 응답 확인")
        @Test
        public void re_member() throws Exception {
            // given -0. tester
            User target = withdrawal();
            User admin = admin();
            
            // 1. 로그인 시도
            SignInRequest signInRequest = SignInRequest.of(target.getEmail(), "1234");
            var dateTime = DateTimeUtil.dateTimeCalculate(target.getModifiedAt());
            mvc.perform(post("/login")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(signInRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(String.format(
                            EXCEPTION_WITHDRAWAL, dateTime.days(), dateTime.hours(), dateTime.minutes())))
                    .andDo(print());
            
            // 2. `member`로 재설정
            setAuthentication(admin);
            
            mvc.perform(put("/user/role")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(UserGradeUpdateRequest.builder()
                                    .id(target.getId())
                                    .role(TYPE_MEMBER).build())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(String.format(
                            RESPONSE_USER_GRADE_UPDATE, target.getNickname(), TYPE_MEMBER)))
                    .andDo(print());
            
            TestSecurityContextHolder.clearContext();
            
            // when-then
            mvc.perform(post("/login")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(SignInRequest.of(
                                    target.getEmail(), "1234"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(String.format(
                            RESPONSE_SUCCESS_FORMAT_SIGN_IN, target.getNickname(), target.getEmail())))
                    .andDo(print());
        }
        
        
        @DisplayName("탈퇴 회원을 관리자가 다시 탈퇴 처리할 경우 응답 확인")
        @Test
        public void re_withdrawal() throws Exception {
            // given
            User target = withdrawal();
            User admin = admin();
            
            setAuthentication(admin);
            
            // when-then
            mvc.perform(put("/user/role")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(UserGradeUpdateRequest.builder()
                                    .id(target.getId())
                                    .role(TYPE_WITHDRAWAL).build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_ALREADY_WITHDRAWN_MEMBER))
                    .andDo(print());
        }
        
        
        @DisplayName("탈퇴 유예기간이 끝나고 로그인 시도 (cron 삭제 실행 전)")
        @Test
        public void hoxy() throws Exception {
            // given
            User target = withdrawal();
            
            Thread.sleep(Duration.of(EXPIRE_TIME, EXPIRE_TIME_UNIT));
            
            // when-then
            SignInRequest signInRequest = SignInRequest.of(target.getEmail(), "1234");
            var period = DateTimeUtil.dateTimeCalculate(target.getModifiedAt());
            mvc.perform(post("/login")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(signInRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(String.format(
                            EXCEPTION_WITHDRAWAL, period.days(), period.hours(), period.minutes())))
                    .andDo(print());
        }
        
        @Disabled("article server 실행되어있어야 통과")
        @DisplayName("탈퇴 유예기간이 끝나고 로그인 시도 (cron 삭제 실행 후 : cron 설정 - 5초마다 실행)")
        @Test
        public void hoxy_scheduler() throws Exception {
            // given
            User target = withdrawal();
            
            User member1 = member_1();
            member1.setWithdrawal(true);
            member1.setRole(target.getRole());
            userRepository.save(member1);
            
            User member2 = member_2();
            member2.setWithdrawal(true);
            member2.setRole(target.getRole());
            userRepository.save(member2);
            
            Thread.sleep(Duration.of(EXPIRE_TIME + 11 , EXPIRE_TIME_UNIT));
            
            // when-then
            hoxy_scheduler_physicalDeletedUserLogin(SignInRequest.of(target.getEmail(), "1234"));
            hoxy_scheduler_physicalDeletedUserLogin(SignInRequest.of(member1.getEmail(), "1234"));
            hoxy_scheduler_physicalDeletedUserLogin(SignInRequest.of(member2.getEmail(), "1234"));
        }
        
        private void hoxy_scheduler_physicalDeletedUserLogin(SignInRequest signInRequest) throws Exception {
            mvc.perform(post("/login")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(signInRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_SIGNIN_FAIL))
                    .andDo(print());
        }
        
    }
    
    
    @BeforeEach public void initUser() {
        Role roleAdmin = roleRepository.findByGrade(TYPE_ADMIN);
        Role roleMember = roleRepository.findByGrade(TYPE_MEMBER);
        Role roleWithdrawal = roleRepository.findByGrade(TYPE_WITHDRAWAL);
        
        String password = passwordEncoder.encode("1234");
        
        jwtUserInfoRepository.save(jwtUserInfo(
                userRepository.save(User.builder()
                        .email(ADMIN_USERNAME)
                        .password(password)
                        .nickname("admin")
                        .withdrawal(false)
                        .role(roleAdmin).build())
        ));
        jwtUserInfoRepository.save(jwtUserInfo(
                userRepository.save(User.builder()
                        .email(MEMBER_1_USERNAME)
                        .password(password)
                        .nickname("mem1")
                        .withdrawal(false)
                        .role(roleMember).build())
        ));
        jwtUserInfoRepository.save(jwtUserInfo(
                userRepository.save(User.builder()
                        .email(MEMBER_2_USERNAME)
                        .password(password)
                        .nickname("mem2")
                        .withdrawal(false)
                        .role(roleMember).build())
        ));
        jwtUserInfoRepository.save(jwtUserInfo(
                userRepository.save(User.builder()
                        .email(WITHDRAWAL_USERNAME)
                        .password(password)
                        .nickname("탈퇴🖐️")
                        .withdrawal(true)
                        .role(roleWithdrawal).build())
        ));
    }
    @AfterEach  public void clean() {
        userRepository.deleteAll();
        jwtUserInfoRepository.deleteAll();
    }
    
    private User admin() {
        return userRepository.findByEmail(ADMIN_USERNAME).get();
    }
    private User member_1() {
        return userRepository.findByEmail(MEMBER_1_USERNAME).get();
    }
    private User member_2() {
        return userRepository.findByEmail(MEMBER_2_USERNAME).get();
    }
    private User withdrawal() {
        return userRepository.findByEmail(WITHDRAWAL_USERNAME).get();
    }
    
    private JwtUserInfo jwtUserInfo(User savedWithdrawal) {
        return JwtUserInfo.builder()
                .id(savedWithdrawal.getId())
                .email(savedWithdrawal.getEmail())
                .nickname(savedWithdrawal.getNickname())
                .grade("ROLE_" + savedWithdrawal.getRole().getGrade()).build();
    }
    
}