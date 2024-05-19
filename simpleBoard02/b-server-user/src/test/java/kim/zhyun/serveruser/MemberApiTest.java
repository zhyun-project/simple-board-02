package kim.zhyun.serveruser;

import com.fasterxml.jackson.databind.ObjectMapper;
import kim.zhyun.jwt.common.constants.JwtConstants;
import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.serveruser.common.message.ResponseMessage;
import kim.zhyun.serveruser.domain.member.controller.model.UserGradeUpdateRequest;
import kim.zhyun.serveruser.domain.member.controller.model.UserUpdateRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("전체 테스트 실행시 무시되도록 설정 켜야 됨")
@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemberApiTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    
    
    @Order(0)
    @DisplayName("로그인")
    @ParameterizedTest
    @MethodSource
    void login(Map<String, String> userInfo) throws Exception {
        mvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userInfo))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andDo(print());
    }
    static Stream<Map<String, String>> login() {
        return Stream.of(
                Map.of("email", "gimwlgus@gmail.com", "password", "password"),
                Map.of("email", "member1@email.mail", "password", "password"),
                Map.of("email", "member2@email.mail", "password", "password"),
                Map.of("email", "withdrawal@email.mail", "password", "password")
        );
    }
    
    
    @Order(1)
    @DisplayName("모든 계정 정보 조회 성공: admin 권한")
    @ParameterizedTest
    @ValueSource(strings = {
            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6NCwiZXhwIjoxNzE4NDUzNjUwfQ.KlTonkMaLz-Ot2G5Xv0nDxA8pd8QGWD6yymVZw4QIYpyHNo7CbkPw7as75vCviX1cBGhgc2PJU-1sTveOJ7m4g",
//            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BkYXVtLm5ldCIsImlkIjoyLCJleHAiOjE3MTgxMDA3MDl9.YrX_ZPRcQTFTPEZq7pUujvn_PahT-Dki8Qv-SYuohGlqHw8Y5XqenCSSsKQ7tHTAYcYucWYt1gG8m0-1KXuiZg"
    })
    void findAll_success(String headerJwt) throws Exception {
        mvc.perform(get("/all")
                        .header(JwtConstants.JWT_HEADER, headerJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("전체 계정 상세 조회"))
                .andDo(print());
    }
    
    @Order(1)
    @DisplayName("모든 계정 정보 조회 실패: member 권한")
    @ParameterizedTest
    @ValueSource(strings = {
//            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MSwiZXhwIjoxNzE4MTAwNzA4fQ.vyJMTJ1N3-vLjMC3GO93McMZ-e2gGqgXDmheUgK8o8D9lmv-wkCgyUQMcgRpoD-2YdzP_B-qHg0ZCmORjofPNQ",
            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BkYXVtLm5ldCIsImlkIjoyLCJleHAiOjE3MTgxMDA3MDl9.YrX_ZPRcQTFTPEZq7pUujvn_PahT-Dki8Qv-SYuohGlqHw8Y5XqenCSSsKQ7tHTAYcYucWYt1gG8m0-1KXuiZg"
    })
    void findAll_fail(String headerJwt) throws Exception {
        mvc.perform(get("/all")
                        .header(JwtConstants.JWT_HEADER, headerJwt))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("권한이 없습니다."))
                .andDo(print());
    }
    
    
    @Order(1)
    @DisplayName("본인 계정 정보 조회")
    @ParameterizedTest
    @ValueSource(strings = {
            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MSwiZXhwIjoxNzE4MTAwNzA4fQ.vyJMTJ1N3-vLjMC3GO93McMZ-e2gGqgXDmheUgK8o8D9lmv-wkCgyUQMcgRpoD-2YdzP_B-qHg0ZCmORjofPNQ",
//            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BkYXVtLm5ldCIsImlkIjoyLCJleHAiOjE3MTgxMDA3MDl9.YrX_ZPRcQTFTPEZq7pUujvn_PahT-Dki8Qv-SYuohGlqHw8Y5XqenCSSsKQ7tHTAYcYucWYt1gG8m0-1KXuiZg"
    })
    void findById_admin(String headerJwt) throws Exception {
        
        
        mvc.perform(get("/{id}", 1L)
                        .header(JwtConstants.JWT_HEADER, headerJwt))
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value("계정 상세 조회"))
                .andDo(print());
        
    }
    
    
    @Order(2)
    @DisplayName("본인 계정 정보 수정")
    @ParameterizedTest
    @ValueSource(strings = {
//            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MSwiZXhwIjoxNzE4MTAwNzA4fQ.vyJMTJ1N3-vLjMC3GO93McMZ-e2gGqgXDmheUgK8o8D9lmv-wkCgyUQMcgRpoD-2YdzP_B-qHg0ZCmORjofPNQ",
            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BkYXVtLm5ldCIsImlkIjoyLCJleHAiOjE3MTgxMDA3MDl9.YrX_ZPRcQTFTPEZq7pUujvn_PahT-Dki8Qv-SYuohGlqHw8Y5XqenCSSsKQ7tHTAYcYucWYt1gG8m0-1KXuiZg"
    })
    void updateById_success(String headerJwt) throws Exception {
        // given
        long requestUserId = 2L;
        String requestEmail = "gimwlgus@daum.net";
        String requestNickname = "닉넴업뎃😌";
        String requestPassword = "update password";
        
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .id(requestUserId)
                .email(requestEmail)
                .nickname(requestNickname)
                .password(requestPassword)
                .build();
        
        
        // when - then
        MockHttpSession session = new MockHttpSession();
        
        // 닉네임 중복확인
        mvc.perform(
                get("/check")
                        .param("nickname", requestNickname)
                        .session(session)
                )
                .andDo(print());
        
        // 닉네임, 비밀번호 변경
        String responseMessage = ResponseMessage.RESPONSE_USER_INFO_UPDATE.formatted(requestNickname);
        mvc.perform(
                        put("/{id}", requestUserId)
                                .header(JwtConstants.JWT_HEADER, headerJwt)
                                .session(session)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(userUpdateRequest))
                )
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value(responseMessage))
                .andDo(print());
    }
    
    
    @Order(3)
    @DisplayName("계정 권한 수정 - 성공: admin 권한자")
    @ParameterizedTest
    @ValueSource(strings = {
            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6NCwiZXhwIjoxNzE4NDUzNjUwfQ.KlTonkMaLz-Ot2G5Xv0nDxA8pd8QGWD6yymVZw4QIYpyHNo7CbkPw7as75vCviX1cBGhgc2PJU-1sTveOJ7m4g",
//            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BkYXVtLm5ldCIsImlkIjoyLCJleHAiOjE3MTgxMDA3MDl9.YrX_ZPRcQTFTPEZq7pUujvn_PahT-Dki8Qv-SYuohGlqHw8Y5XqenCSSsKQ7tHTAYcYucWYt1gG8m0-1KXuiZg"
    })
    void updateByIdAndRole(String headerJwt) throws Exception {
        // given
        long requestUserId = 5L;
        
        UserGradeUpdateRequest userGradeUpdateRequest = UserGradeUpdateRequest.builder()
                .id(requestUserId)
                .role(RoleType.TYPE_MEMBER)
                .build();
        
        
        // when - then
        mvc.perform(
                        put("/role")
                                .header(JwtConstants.JWT_HEADER, headerJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(userGradeUpdateRequest))
                )
                .andExpect(jsonPath("$.status").value(true))
                .andDo(print());
    }
    
    @Order(4)
    @DisplayName("계정 권한 수정 - 실패: member 권한자")
    @ParameterizedTest
    @ValueSource(strings = {
//            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MSwiZXhwIjoxNzE4MTAwNzA4fQ.vyJMTJ1N3-vLjMC3GO93McMZ-e2gGqgXDmheUgK8o8D9lmv-wkCgyUQMcgRpoD-2YdzP_B-qHg0ZCmORjofPNQ",
            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BkYXVtLm5ldCIsImlkIjoyLCJleHAiOjE3MTgxMDA3MDl9.YrX_ZPRcQTFTPEZq7pUujvn_PahT-Dki8Qv-SYuohGlqHw8Y5XqenCSSsKQ7tHTAYcYucWYt1gG8m0-1KXuiZg"
    })
    void updateByIdAndRole_fail(String headerJwt) throws Exception {
        // given
        long requestUserId = 1L;
        
        UserGradeUpdateRequest userGradeUpdateRequest = UserGradeUpdateRequest.builder()
                .id(requestUserId)
                .role(RoleType.TYPE_MEMBER)
                .build();
        
        
        // when - then
        mvc.perform(
                        put("/role")
                                .header(JwtConstants.JWT_HEADER, headerJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(userGradeUpdateRequest))
                )
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("권한이 없습니다."))
                .andDo(print());
    }
    
    
    
    @Order(5)
    @DisplayName("로그아웃")
    @ParameterizedTest
    @ValueSource(strings = {
//            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MSwiZXhwIjoxNzE4MTAwNzA4fQ.vyJMTJ1N3-vLjMC3GO93McMZ-e2gGqgXDmheUgK8o8D9lmv-wkCgyUQMcgRpoD-2YdzP_B-qHg0ZCmORjofPNQ",
            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BkYXVtLm5ldCIsImlkIjoyLCJleHAiOjE3MTgxMDA3MDl9.YrX_ZPRcQTFTPEZq7pUujvn_PahT-Dki8Qv-SYuohGlqHw8Y5XqenCSSsKQ7tHTAYcYucWYt1gG8m0-1KXuiZg"
    })
    void logout(String headerJwt) throws Exception {
        // when
        mvc.perform(
                        post("/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(JwtConstants.JWT_HEADER, headerJwt)
                )
                .andExpect(jsonPath("$.status").value(true))
                .andDo(print());
    }
    
    
    @Order(6)
    @DisplayName("회원 탈퇴 - 토큰에서 회원 권한 읽은 후 로직 진행")
    @ParameterizedTest
    @ValueSource(strings = {
//            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ3aXRoZHJhd2FsQGVtYWlsLm1haWwiLCJpZCI6NywiZXhwIjoxNzE4NDUzNjUxfQ.jQJAMNB204ul2QBcii26fcVkSQmwP16-q02LZfJDnV9M7qhoZukkug4oDlg60_-jHnFCSzmjTj9Ujx1VHYieWw",
            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtZW1iZXIyQGVtYWlsLm1haWwiLCJpZCI6NiwiZXhwIjoxNzE4NDUzNjUwfQ.XgVUMIdJdD8nEtp5lrPwqtsCpuUQgP_4N6eGpYeLO4aaWb252_rylqf132Sp7VSpTKdY3772EGQSbtpnP_RcOA"
    })
    void withdrawal(String headerJwt) throws Exception {
        // when
        mvc.perform(
                        post("/withdrawal")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(JwtConstants.JWT_HEADER, headerJwt)
                )
                .andExpect(jsonPath("$.status").value(true))
                .andDo(print());
    }
    
}
