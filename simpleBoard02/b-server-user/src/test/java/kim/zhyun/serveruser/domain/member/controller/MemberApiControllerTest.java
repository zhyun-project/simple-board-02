package kim.zhyun.serveruser.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kim.zhyun.jwt.common.constants.JwtConstants;
import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.serveruser.container.RedisTestContainer;
import kim.zhyun.serveruser.domain.member.controller.model.UserGradeUpdateRequest;
import kim.zhyun.serveruser.domain.member.controller.model.UserUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(0)
@ExtendWith(RedisTestContainer.class)
@AutoConfigureMockMvc
@SpringBootTest
class MemberApiControllerTest {
    
    @Autowired ObjectMapper objectMapper;
    @Autowired MockMvc mvc;
    
    
    
    @DisplayName("모든 계정 정보 조회")
    @Nested
    class FindAllCase {
        @DisplayName("성공: admin 권한")
        @WithMockUser(roles = RoleType.TYPE_ADMIN)
        @Test
        void findAll_success() throws Exception {
            mvc.perform(get("/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andDo(print());
        }
        
        @DisplayName("실패: member 권한")
        @Test
        @WithMockUser(roles = RoleType.TYPE_MEMBER)
        void findAll_fail_member() throws Exception {
            mvc.perform(get("/all"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andDo(print());
        }
        
        @DisplayName("실패: withdrawal 권한")
        @Test
        @WithMockUser(roles = RoleType.TYPE_WITHDRAWAL)
        void findAll_fail_withdrawal() throws Exception {
            mvc.perform(get("/all"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andDo(print());
        }
        
        @DisplayName("실패: anonymous")
        @Test
        @WithAnonymousUser
        void findAll_fail_anonymous() throws Exception {
            mvc.perform(get("/all"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andDo(print());
        }
    }
    
    
    @DisplayName("본인 계정 정보 조회")
    @Nested
    class FindById {
        
        String successMessage = "일치하는 사용자 정보가 없습니다.";
        String failMessage1 = "권한이 없습니다.";
        String failMessage2 = "로그인이 필요합니다.";
        
        @DisplayName("admin 권한")
        @Test
        @WithMockUser(roles = RoleType.TYPE_ADMIN)
        void findById_admin() throws Exception {
            
            mvc.perform(get("/{id}", 1L))
                    .andExpect(jsonPath("$.message").value(successMessage))
                    .andDo(print());
            
        }
        
        @DisplayName("member 권한")
        @Test
        @WithMockUser(roles = RoleType.TYPE_MEMBER)
        void findById_member() throws Exception {
            
            mvc.perform(get("/{id}", 1L))
                    .andExpect(jsonPath("$.message").value(successMessage))
                    .andDo(print());
            
        }
        
        @DisplayName("탈퇴자")
        @Test
        @WithMockUser(roles = RoleType.TYPE_WITHDRAWAL)
        void findById_withdrawal() throws Exception {
            
            mvc.perform(get("/{id}", 1L))
                    .andExpect(jsonPath("$.message").value(failMessage1))
                    .andDo(print());
            
        }
        
        @DisplayName("비회원")
        @Test
        @WithAnonymousUser
        void findById_anonymous() throws Exception {
            
            mvc.perform(get("/{id}", 1L))
                    .andExpect(jsonPath("$.message").value(failMessage2))
                    .andDo(print());
            
        }
    }
    
    
    @DisplayName("본인 계정 정보 수정")
    @Nested
    class UpdateById {
        
        String successMessage = "일치하는 사용자 정보가 없습니다.";
        String failMessage = "권한이 없습니다.";
        
        
        
        @DisplayName("성공")
        @ParameterizedTest
        @ValueSource(strings = {
                RoleType.ROLE_ADMIN,
                RoleType.ROLE_MEMBER
        })
        void updateById_success(String role) throws Exception {
            // given
            long requestUserId = 1L;
            String requestEmail = "email@mail.ail";
            String requestNickname = "닉넴";
            String requestPassword = "password";
            
            setSecurityContext(requestUserId, requestEmail, requestNickname, role);
            
            UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                    .id(requestUserId)
                    .email(requestEmail)
                    .nickname(requestNickname)
                    .password(requestPassword)
                    .build();
            
            
            // when - then
            mvc.perform(
                    put("/{id}", requestUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(userUpdateRequest))
                    )
                    .andExpect(jsonPath("$.message").value(successMessage))
                    .andDo(print());
            
        }
        
        @DisplayName("실패")
        @ParameterizedTest
        @ValueSource(strings = {
                RoleType.ROLE_WITHDRAWAL,
                "ROLE_ANONYMOUS",
        })
        void updateById(String role) throws Exception {
            // given
            long requestUserId = 1L;
            String requestEmail = "email@mail.ail";
            String requestNickname = "닉넴";
            String requestPassword = "password";
            
            setSecurityContext(requestUserId, requestEmail, requestNickname, role);
            
            UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                    .id(requestUserId)
                    .email(requestEmail)
                    .nickname(requestNickname)
                    .password(requestPassword)
                    .build();
            
            
            // when - then
            mvc.perform(
                    put("/{id}", requestUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(userUpdateRequest))
                    )
                    .andExpect(jsonPath("$.message").value(failMessage))
                    .andDo(print());
            
        }
    }
    
    
    @DisplayName("계정 권한 수정")
    @Nested
    class UpdateByIdAndRole {
        String successMessage = "일치하는 사용자 정보가 없습니다.";
        String failMessage    = "권한이 없습니다.";
        
        @DisplayName("성공")
        @ParameterizedTest
        @ValueSource(strings = {
                RoleType.ROLE_ADMIN
        })
        void updateByIdAndRole(String roleType) throws Exception {
            // given
            long requestUserId = 1L;
            String requestEmail = "email@mail.ail";
            String requestNickname = "닉넴";
            
            setSecurityContext(requestUserId, requestEmail, requestNickname, roleType);
            
            UserGradeUpdateRequest userGradeUpdateRequest = UserGradeUpdateRequest.builder()
                    .id(10L)
                    .role(RoleType.TYPE_WITHDRAWAL)
                    .build();
            
            
            // when - then
            mvc.perform(
                            put("/role")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(userGradeUpdateRequest))
                    )
                    .andExpect(jsonPath("$.message").value(successMessage))
                    .andDo(print());
        }
        
        @DisplayName("실패")
        @ParameterizedTest
        @ValueSource(strings = {
                RoleType.ROLE_MEMBER,
                RoleType.ROLE_WITHDRAWAL,
                "ROLE_ANONYMOUS"
        })
        void updateByIdAndRole_fail(String roleType) throws Exception {
            // given
            long requestUserId = 1L;
            String requestEmail = "email@mail.ail";
            String requestNickname = "닉넴";
            
            setSecurityContext(requestUserId, requestEmail, requestNickname, roleType);
            
            UserGradeUpdateRequest userGradeUpdateRequest = UserGradeUpdateRequest.builder()
                    .id(10L)
                    .role(RoleType.TYPE_WITHDRAWAL)
                    .build();
            
            
            // when - then
            mvc.perform(
                    put("/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(userGradeUpdateRequest))
                    )
                    .andExpect(jsonPath("$.message").value(failMessage))
                    .andDo(print());
        }
    }
    
    
    @DisplayName("로그아웃 - 토큰에서 회원 권한 읽은 후 로직 진행")
    @Test
    void logout() throws Exception {
        // given
        String jwt = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MiwiZXhwIjoxNzA3NDE3NTc2fQ.3p1pk4Il-lmtq8jlYS5xyKd_78ehPYt-WyVHkN6XrvYq6fGCfnLdRmZrPOvC52nZBcYfGLzz7wUxR8dXOzlQug";
        
        // when
        mvc.perform(
                post("/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(JwtConstants.JWT_HEADER, jwt)
                )
                .andDo(print());
    }
    
    
    @DisplayName("회원 탈퇴 - 토큰에서 회원 권한 읽은 후 로직 진행")
    @Test
    void withdrawal() throws Exception {
        // given
        String jwt = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MiwiZXhwIjoxNzA3NDE3NTc2fQ.3p1pk4Il-lmtq8jlYS5xyKd_78ehPYt-WyVHkN6XrvYq6fGCfnLdRmZrPOvC52nZBcYfGLzz7wUxR8dXOzlQug";
        
        // when
        mvc.perform(
                post("/withdrawal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(JwtConstants.JWT_HEADER, jwt)
                )
                .andDo(print());
    }
    
    
    private void setSecurityContext(long requestUserId, String requestEmail, String requestNickname, String roleType) {
        TestSecurityContextHolder.getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken
                                .authenticated(
                                        JwtUserInfoEntity.builder()
                                                .id(requestUserId)
                                                .email(requestEmail)
                                                .nickname(requestNickname)
                                                .grade(roleType)
                                                .build(),
                                        null,
                                        List.of(new SimpleGrantedAuthority(roleType))
                                )
                );
    }
}