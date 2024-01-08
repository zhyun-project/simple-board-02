package kim.zhyun.serveruser.controller;

import kim.zhyun.serveruser.config.SecurityConfig;
import kim.zhyun.serveruser.data.entity.Role;
import kim.zhyun.serveruser.data.entity.User;
import kim.zhyun.serveruser.repository.RoleRepository;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_PERMISSION;
import static kim.zhyun.serveruser.data.message.ResponseMessage.RESPONSE_USER_REFERENCE_ALL;
import static kim.zhyun.serveruser.data.message.ResponseMessage.RESPONSE_USER_REFERENCE_ME;
import static kim.zhyun.serveruser.data.type.RoleType.*;
import static kim.zhyun.serveruser.util.TestSecurityUser.setAuthentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("member controller test")
@ExtendWith(RedisTestContainer.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@SpringBootTest
class MemberControllerTest {
    private final String TEST_USERNAME = "gimwlgus@daum.net";
    
    private final MockMvc mvc;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    public MemberControllerTest(@Autowired MockMvc mvc,
                                @Autowired UserRepository userRepository,
                                @Autowired RoleRepository roleRepository) {
        this.mvc = mvc;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        
        initUser();
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
        @WithMockUser(roles = TYPE_MEMBER)
        public void find_by_id_from_me() throws Exception {
            setAuthentication(TEST_USERNAME, "test user 1");
            
            mvc.perform(get("/user/{id}", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(RESPONSE_USER_REFERENCE_ME))
                    .andDo(print());
        }
        
        @DisplayName("다른 사람 계정 조회 - 실패")
        @Test
        @WithMockUser(roles = TYPE_MEMBER)
        public void find_by_id_from_others() throws Exception {
            setAuthentication("others@gmail.com", "test user 1");
            
            mvc.perform(get("/user/{id}", 1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(EXCEPTION_PERMISSION))
                    .andDo(print());
        }
    }
    
    
    
    public void initUser() {
        Role role = roleRepository.findByGrade(TYPE_ADMIN);
        userRepository.save(User.builder()
                .id(1L)
                .email(TEST_USERNAME)
                .password("1234")
                .nickname("test user")
                .role(role).build());
    }
    
}