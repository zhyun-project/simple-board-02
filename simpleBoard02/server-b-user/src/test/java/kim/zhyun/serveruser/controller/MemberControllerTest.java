package kim.zhyun.serveruser.controller;

import kim.zhyun.serveruser.config.SecurityConfig;
import kim.zhyun.serveruser.data.NicknameDto;
import kim.zhyun.serveruser.data.UserUpdateRequest;
import kim.zhyun.serveruser.data.entity.Role;
import kim.zhyun.serveruser.data.entity.User;
import kim.zhyun.serveruser.repository.RoleRepository;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.service.NicknameReserveService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.*;
import static kim.zhyun.serveruser.data.message.ResponseMessage.*;
import static kim.zhyun.serveruser.data.type.RoleType.*;
import static kim.zhyun.serveruser.util.TestSecurityUser.setAuthentication;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    
    private final MockMvc mvc;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NicknameReserveService nicknameReserveService;
    private final PasswordEncoder passwordEncoder;
    public MemberControllerTest(@Autowired MockMvc mvc,
                                @Autowired UserRepository userRepository,
                                @Autowired RoleRepository roleRepository,
                                @Autowired NicknameReserveService nicknameReserveService,
                                @Autowired PasswordEncoder passwordEncoder) {
        this.mvc = mvc;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.nicknameReserveService = nicknameReserveService;
        this.passwordEncoder = passwordEncoder;
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
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(UserUpdateRequest.builder()
                                    .id(other.getId())
                                    .email(other.getEmail())
                                    .password("왔다갑니다").build())))
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
                                .contentType(MediaType.APPLICATION_JSON)
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
                                .contentType(MediaType.APPLICATION_JSON)
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
                                .contentType(MediaType.APPLICATION_JSON)
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
                                .contentType(MediaType.APPLICATION_JSON)
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
                                .contentType(MediaType.APPLICATION_JSON)
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
                                .contentType(MediaType.APPLICATION_JSON)
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
                                .contentType(MediaType.APPLICATION_JSON)
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
                                .contentType(MediaType.APPLICATION_JSON)
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
    
    @BeforeEach public void initUser() {
        Role roleAdmin = roleRepository.findByGrade(TYPE_ADMIN);
        Role roleMember = roleRepository.findByGrade(TYPE_MEMBER);
        
        userRepository.save(User.builder()
                .email(ADMIN_USERNAME)
                .password("1234")
                .nickname("admin")
                .role(roleAdmin).build());
        userRepository.save(User.builder()
                .email(MEMBER_1_USERNAME)
                .password("1234")
                .nickname("mem1")
                .role(roleMember).build());
        userRepository.save(User.builder()
                .email(MEMBER_2_USERNAME)
                .password("1234")
                .nickname("mem2")
                .role(roleMember).build());
    }
    @AfterEach public void clean() {
        userRepository.deleteAll();
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
    
}