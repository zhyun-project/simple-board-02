package kim.zhyun.serveruser.domain.member.controller;

import kim.zhyun.jwt.common.constants.JwtConstants;
import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.jwt.domain.dto.JwtAuthentication;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.exception.message.CommonExceptionMessage;
import kim.zhyun.jwt.filter.JwtFilter;
import kim.zhyun.serveruser.common.message.ResponseMessage;
import kim.zhyun.serveruser.config.TestSecurityConfig;
import kim.zhyun.serveruser.domain.member.business.MemberBusiness;
import kim.zhyun.serveruser.domain.member.controller.model.UserGradeUpdateRequest;
import kim.zhyun.serveruser.domain.member.controller.model.UserResponse;
import kim.zhyun.serveruser.domain.member.controller.model.UserUpdateRequest;
import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
import kim.zhyun.serveruser.filter.AuthenticationFilter;
import kim.zhyun.serveruser.filter.SessionCheckFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Import(TestSecurityConfig.class)
@WebMvcTest(
        controllers = MemberApiController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        AuthenticationFilter.class,
                        JwtFilter.class,
                        SessionCheckFilter.class
                })
)
class MemberApiControllerTest {
    
    @MockBean MemberBusiness memberBusiness;
    
    @Autowired MockMvc mvc;
    ObjectMapper objectMapper = new ObjectMapper();
    
    
    @DisplayName("모든 계정 정보 조회")
    @Nested
    class FindAllCase {
        
        @DisplayName("성공: admin 권한")
        @WithMockUser(roles = RoleType.TYPE_ADMIN)
        @Test
        void findAll_success() throws Exception {
            // given
            List<UserResponse> userResponseList = List.of(
                    getUserResponse(1L, "email@mail.ail", "user김"),
                    getUserResponse(2L, "name@email.mail", "김user")
            );
            
            given(memberBusiness.findAll()).willReturn(userResponseList);

            String responseMessage = ResponseMessage.RESPONSE_USER_REFERENCE_ALL;

            
            // when-then
            mvc.perform(get("/member/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andExpect(jsonPath("$.result.length()").value(userResponseList.size()))
                    .andExpect(jsonPath("$.result[0].email").value(userResponseList.get(0).getEmail()))
                    .andExpect(jsonPath("$.result[1].email").value(userResponseList.get(1).getEmail()))
                    .andDo(print());
        }
        
        @DisplayName("실패: member 권한")
        @Test
        @WithMockUser(roles = RoleType.TYPE_MEMBER)
        void findAll_fail_member() throws Exception {
            // given
            String responseMessage = CommonExceptionMessage.EXCEPTION_PERMISSION;
            
            
            // when-then
            mvc.perform(get("/member/all"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
        
        @DisplayName("실패: withdrawal 권한")
        @Test
        @WithMockUser(roles = RoleType.TYPE_WITHDRAWAL)
        void findAll_fail_withdrawal() throws Exception {
            // given
            String responseMessage = CommonExceptionMessage.EXCEPTION_PERMISSION;
            
            
            // when-then
            mvc.perform(get("/member/all"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
        
        @DisplayName("실패: anonymous")
        @Test
        @WithAnonymousUser
        void findAll_fail_anonymous() throws Exception {
            // given
            String responseMessage = CommonExceptionMessage.EXCEPTION_AUTHENTICATION;
            
            
            // when-then
            mvc.perform(get("/member/all"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
    }
    
    
    @DisplayName("본인 계정 정보 조회")
    @Nested
    class FindById {
        
        String successMessage = ResponseMessage.RESPONSE_USER_REFERENCE_ME;
        String failMessageWithPermission = CommonExceptionMessage.EXCEPTION_PERMISSION;
        
        @DisplayName("admin 권한")
        @Test
        @WithMockUser(roles = RoleType.TYPE_ADMIN)
        void findById_admin() throws Exception {
            // given
            long userId = 1L;
            String email = "admin@email.mail";
            String nickname = "관리자";
            
            setSecurityContext(userId, email, nickname, RoleType.ROLE_ADMIN);
            
            UserResponse userResponse = getUserResponse(userId, email, nickname);
            given(memberBusiness.findById(userId)).willReturn(userResponse);
            
            
            // when - then
            mvc.perform(get("/member/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(successMessage))
                    .andExpect(jsonPath("$.result.id").value(userResponse.getId()))
                    .andExpect(jsonPath("$.result.email").value(userResponse.getEmail()))
                    .andExpect(jsonPath("$.result.nickname").value(userResponse.getNickname()))
                    .andDo(print());
        }
        
        @DisplayName("member 권한")
        @Test
        @WithMockUser(roles = RoleType.TYPE_MEMBER)
        void findById_member() throws Exception {
            // given
            long userId = 1L;
            String email = "member@email.mail";
            String nickname = "회원";
            
            setSecurityContext(userId, email, nickname, RoleType.ROLE_MEMBER);
            
            UserResponse userResponse = getUserResponse(userId, email, nickname);
            given(memberBusiness.findById(userId)).willReturn(userResponse);
            
            
            // when - then
            mvc.perform(get("/member/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(successMessage))
                    .andExpect(jsonPath("$.result.id").value(userResponse.getId()))
                    .andExpect(jsonPath("$.result.email").value(userResponse.getEmail()))
                    .andExpect(jsonPath("$.result.nickname").value(userResponse.getNickname()))
                    .andDo(print());
        }
        
        @DisplayName("탈퇴자")
        @Test
        @WithMockUser(roles = RoleType.TYPE_WITHDRAWAL)
        void findById_withdrawal() throws Exception {
            // given
            long userId = 1L;
            String email = "withdrawal@email.mail";
            String nickname = "탈퇴자";
            
            setSecurityContext(userId, email, nickname, RoleType.ROLE_WITHDRAWAL);
            
            UserResponse userResponse = getUserResponse(userId, email, nickname);
            given(memberBusiness.findById(userId)).willReturn(userResponse);
            
            
            // when - then
            mvc.perform(get("/member/{id}", userId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(failMessageWithPermission))
                    .andDo(print());
        }
        
        @DisplayName("비회원")
        @Test
        @WithAnonymousUser
        void findById_anonymous() throws Exception {
            // given
            long userId = 1L;
            String email = "member@email.mail";
            String nickname = "김아무개";
            
            setSecurityContext(userId, email, nickname, "ROLE_ANONYMOUS");
            
            UserResponse userResponse = getUserResponse(userId, email, nickname);
            given(memberBusiness.findById(userId)).willReturn(userResponse);
            
            
            // when - then
            mvc.perform(get("/member/{id}", userId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(failMessageWithPermission))
                    .andDo(print());
        }
    }
    
    
    
    @DisplayName("본인 계정 정보 수정")
    @Nested
    class UpdateById {
        
        String successMessage = ResponseMessage.RESPONSE_USER_INFO_UPDATE;
        String failMessageWithPermission = CommonExceptionMessage.EXCEPTION_PERMISSION;
        
        
        
        @DisplayName("성공")
        @ParameterizedTest
        @ValueSource(strings = {
                RoleType.ROLE_ADMIN,
                RoleType.ROLE_MEMBER,
        })
        void updateById_success(String role) throws Exception {
            // given
            long requestUserId = 1L;
            String requestEmail = "email@mail.ail";
            String requestNickname = "닉넴";
            String requestPassword = "password";
            
            MockHttpSession session = new MockHttpSession();

            setSecurityContext(requestUserId, requestEmail, requestNickname, role);
            
            UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                    .id(requestUserId)
                    .email(requestEmail)
                    .nickname(requestNickname)
                    .password(requestPassword)
                    .build();
            
            String responseMessage = successMessage.formatted(requestNickname);
            given(memberBusiness.updateUserInfo(eq(session.getId()), any(UserUpdateRequest.class))).willReturn(responseMessage);
            
            
            // when - then
            mvc.perform(
                    put("/member/{id}", requestUserId)
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(userUpdateRequest))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
        
        @DisplayName("실패 - 탈퇴자, 비회원")
        @ParameterizedTest
        @ValueSource(strings = {
                RoleType.ROLE_WITHDRAWAL,
                "ROLE_ANONYMOUS",
        })
        void updateById_fail_not_member(String role) throws Exception {
            // given
            long requestUserId = 1L;
            String requestEmail = "email@mail.ail";
            String requestNickname = "닉넴";
            String requestPassword = "password";
            
            setSecurityContext(requestUserId, requestEmail, requestNickname, role);
            
            MockHttpSession session = new MockHttpSession();
            
            UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                    .id(requestUserId)
                    .email(requestEmail)
                    .nickname(requestNickname)
                    .password(requestPassword)
                    .build();
            
            String responseMessage = failMessageWithPermission;
            given(memberBusiness.updateUserInfo(eq(session.getId()), any(UserUpdateRequest.class))).willReturn(responseMessage);
            
            
            // when - then
            mvc.perform(
                            put("/member/{id}", requestUserId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(userUpdateRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(failMessageWithPermission))
                    .andDo(print());
        }
        
        @DisplayName("실패 - 다른 사람 계정")
        @ParameterizedTest
        @ValueSource(strings = {
                RoleType.ROLE_ADMIN,
                RoleType.ROLE_MEMBER,
                RoleType.ROLE_WITHDRAWAL,
                "ROLE_ANONYMOUS",
        })
        void updateById_fail_not_mine(String role) throws Exception {
            // given
            long requestUserId = 1L;
            String requestEmail = "others@mail.ail";
            String requestNickname = "아더";
            String requestPassword = "password";
            
            setSecurityContext(2L, "email@mail.ail", requestNickname, role);
            
            MockHttpSession session = new MockHttpSession();
            
            UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                    .id(requestUserId)
                    .email(requestEmail)
                    .nickname(requestNickname)
                    .password(requestPassword)
                    .build();
            
            String responseMessage = failMessageWithPermission;
            given(memberBusiness.updateUserInfo(eq(session.getId()), any(UserUpdateRequest.class))).willReturn(responseMessage);
            
            
            // when - then
            mvc.perform(
                            put("/member/{id}", requestUserId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(userUpdateRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(failMessageWithPermission))
                    .andDo(print());
        }
    }
    
    
    @DisplayName("계정 권한 수정")
    @Nested
    class UpdateByIdAndRole {
        String successMessage = ResponseMessage.RESPONSE_USER_GRADE_UPDATE;
        String failMessageWithPermission = CommonExceptionMessage.EXCEPTION_PERMISSION;
        
        
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
            
            String responseMessage = successMessage;
            given(memberBusiness.updateUserGrade(any(UserGradeUpdateRequest.class))).willReturn(responseMessage);
            
            
            // when - then
            mvc.perform(
                            put("/member/role")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(userGradeUpdateRequest))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
        
        @DisplayName("실패 - 관리자 권한 아님")
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
            
            String responseMessage = failMessageWithPermission;
            given(memberBusiness.updateUserGrade(any(UserGradeUpdateRequest.class))).willReturn(responseMessage);
            
            
            // when - then
            mvc.perform(
                    put("/member/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(userGradeUpdateRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(responseMessage))
                    .andDo(print());
        }
    }
    
    
    @DisplayName("로그아웃 - 성공")
    @ParameterizedTest
    @ValueSource(strings = {
            RoleType.ROLE_ADMIN,
            RoleType.ROLE_MEMBER
    })
    void logout_success(String role) throws Exception {
        // given
        String jwt = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MiwiZXhwIjoxNzA3NDE3NTc2fQ.3p1pk4Il-lmtq8jlYS5xyKd_78ehPYt-WyVHkN6XrvYq6fGCfnLdRmZrPOvC52nZBcYfGLzz7wUxR8dXOzlQug";
        
        String responseMessage = ResponseMessage.RESPONSE_SUCCESS_FORMAT_SIGN_OUT;
        given(memberBusiness.logout()).willReturn(responseMessage);
        
        setSecurityContext(
                1L, "member@email.mail", "회원", role, jwt
        );
        
        
        // when
        mvc.perform(
                        post("/member/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(JwtConstants.JWT_HEADER, jwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    
    @DisplayName("로그아웃 - 실패: 탈퇴자, 비회원 접근")
    @ParameterizedTest
    @ValueSource(strings = {
            RoleType.ROLE_WITHDRAWAL,
            "ROLE_ANONYMOUS"
    })
    void logout_fail(String role) throws Exception {
        // given
        String jwt = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MiwiZXhwIjoxNzA3NDE3NTc2fQ.3p1pk4Il-lmtq8jlYS5xyKd_78ehPYt-WyVHkN6XrvYq6fGCfnLdRmZrPOvC52nZBcYfGLzz7wUxR8dXOzlQug";
        
        String responseMessage = CommonExceptionMessage.EXCEPTION_PERMISSION;
        given(memberBusiness.logout()).willReturn(responseMessage);
        
        setSecurityContext(
                1L, "bye@email.mail", "떠남", role, jwt
        );
        
        
        // when
        mvc.perform(
                        post("/member/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(JwtConstants.JWT_HEADER, jwt)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    
    @DisplayName("회원 탈퇴 - 성공")
    @ParameterizedTest
    @ValueSource(strings = {
            RoleType.ROLE_ADMIN,
            RoleType.ROLE_MEMBER
    })
    void withdrawal_success(String role) throws Exception {
        // given
        String jwt = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MiwiZXhwIjoxNzA3NDE3NTc2fQ.3p1pk4Il-lmtq8jlYS5xyKd_78ehPYt-WyVHkN6XrvYq6fGCfnLdRmZrPOvC52nZBcYfGLzz7wUxR8dXOzlQug";
        
        String responseMessage = ResponseMessage.RESPONSE_USER_WITHDRAWAL;
        given(memberBusiness.withdrawal()).willReturn(responseMessage);
        
        setSecurityContext(
                1L, "member@email.mail", "회원", role, jwt
        );
        
        
        // when
        mvc.perform(
                        post("/member/withdrawal")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(JwtConstants.JWT_HEADER, jwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    
    @DisplayName("회원 탈퇴 - 실패")
    @ParameterizedTest
    @ValueSource(strings = {
            RoleType.ROLE_WITHDRAWAL,
            "ROLE_ANONYMOUS"
    })
    void withdrawal_fail(String role) throws Exception {
        // given
        String jwt = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MiwiZXhwIjoxNzA3NDE3NTc2fQ.3p1pk4Il-lmtq8jlYS5xyKd_78ehPYt-WyVHkN6XrvYq6fGCfnLdRmZrPOvC52nZBcYfGLzz7wUxR8dXOzlQug";
        
        String responseMessage = CommonExceptionMessage.EXCEPTION_PERMISSION;
        given(memberBusiness.withdrawal()).willReturn(responseMessage);
        
        setSecurityContext(
                1L, "bye@email.mail", "나간사람", role, jwt
        );
        
        
        // when
        mvc.perform(
                        post("/member/withdrawal")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(JwtConstants.JWT_HEADER, jwt)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    
    
    
    private static UserResponse getUserResponse(
            long id, String email, String nickname
    ) {
        return UserResponse.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .role(new RoleEntity())
                .build();
    }

    private void setSecurityContext(long requestUserId, String requestEmail, String requestNickname, String roleType) {
        TestSecurityContextHolder.getContext()
                .setAuthentication(
                        new JwtAuthentication(
                                JwtUserInfoDto.builder()
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
    private void setSecurityContext(long requestUserId, String requestEmail, String requestNickname, String roleType, String credentials) {
        TestSecurityContextHolder.getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken
                                .authenticated(
                                        JwtUserInfoDto.builder()
                                                .id(requestUserId)
                                                .email(requestEmail)
                                                .nickname(requestNickname)
                                                .grade(roleType)
                                                .build(),
                                        credentials,
                                        List.of(new SimpleGrantedAuthority(roleType))
                                )
                );
    }
}