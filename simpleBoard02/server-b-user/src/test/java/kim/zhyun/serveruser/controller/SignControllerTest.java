package kim.zhyun.serveruser.controller;

import kim.zhyun.serveruser.advice.SignUpException;
import kim.zhyun.serveruser.data.SignInRequest;
import kim.zhyun.serveruser.data.SignupRequest;
import kim.zhyun.serveruser.data.entity.User;
import kim.zhyun.serveruser.data.message.ExceptionMessage;
import kim.zhyun.serveruser.data.message.ResponseMessage;
import kim.zhyun.serveruser.data.type.RoleType;
import kim.zhyun.serveruser.repository.RoleRepository;
import kim.zhyun.serveruser.repository.UserRepository;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.service.SignUpService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static kim.zhyun.jwt.data.JwtConstants.JWT_HEADER;
import static kim.zhyun.jwt.data.JwtConstants.JWT_PREFIX;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.*;
import static kim.zhyun.serveruser.data.message.ResponseMessage.SUCCESS_FORMAT_SIGN_IN;
import static kim.zhyun.serveruser.data.message.ResponseMessage.SUCCESS_FORMAT_SIGN_OUT;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(RedisTestContainer.class)
@AutoConfigureMockMvc
@SpringBootTest
class SignControllerTest {
    
    @MockBean
    private SignUpService signupService;
    
    private final MockMvc mvc;
    private final ObjectMapper mapper;
    public SignControllerTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
        this.mapper = new ObjectMapper();
    }
    
    @DisplayName("회원 가입 테스트")
    @Nested
    class SignUpTest {
        private final String EMAIL = "gimwlgus@gmail.com";
        private final String EMAIL_CHANGED = "wlgus@gmail.com";
        private final String NICKNAME = "얼거스";
        private final String NICKNAME_CHANGED = "보거스";
        private final String PASSWORD = "test";
        
        @DisplayName("실패 - 이메일 중복확인 안함")
        @Test
        void fail_email_duplicate_pass() throws Exception {
            // given
            MockHttpSession session = new MockHttpSession();
            final String SESSION_ID = session.getId();
            
            SignupRequest signupRequest = SignupRequest.of(EMAIL, NICKNAME, PASSWORD);
            
            doThrow(new SignUpException(REQUIRE_MAIL_DUPLICATE_CHECK))
                    .when(signupService).saveMember(SESSION_ID, signupRequest);
            
            // when-then
            mvc.perform(post("/sign-up")
                            .content(mapper.writeValueAsString(signupRequest))
                            .contentType(APPLICATION_JSON)
                            .session(session))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("status").value(false))
                    .andExpect(jsonPath("message").value(REQUIRE_MAIL_DUPLICATE_CHECK))
                    .andDo(print());
        }
        
        @DisplayName("실패 - 이메일 다름")
        @Test
        void fail_email_changed() throws Exception {
            // given
            MockHttpSession session = new MockHttpSession();
            final String SESSION_ID = session.getId();
            
            SignupRequest signupRequest = SignupRequest.of(EMAIL, NICKNAME, PASSWORD);
            SignupRequest signupRequestOtherEmail = SignupRequest.of(EMAIL_CHANGED, NICKNAME, PASSWORD);
            
            doNothing().when(signupService).saveMember(SESSION_ID, signupRequest);
            doThrow(new SignUpException(REQUIRE_MAIL_DUPLICATE_CHECK))
                    .when(signupService).saveMember(SESSION_ID, signupRequestOtherEmail);
            
            // when-then
            mvc.perform(post("/sign-up")
                            .content(mapper.writeValueAsString(signupRequestOtherEmail))
                            .contentType(APPLICATION_JSON)
                            .session(session))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("status").value(false))
                    .andExpect(jsonPath("message").value(REQUIRE_MAIL_DUPLICATE_CHECK))
                    .andDo(print());
        }
        
        @DisplayName("실패 - 닉네임 다름")
        @Test
        void fail_nickname_changed() throws Exception {
            // given
            MockHttpSession session = new MockHttpSession();
            final String SESSION_ID = session.getId();
            
            SignupRequest signupRequest = SignupRequest.of(EMAIL, NICKNAME, PASSWORD);
            SignupRequest signupRequestOtherNickname = SignupRequest.of(EMAIL, NICKNAME_CHANGED, PASSWORD);
            
            doNothing().when(signupService).saveMember(SESSION_ID, signupRequest);
            doThrow(new SignUpException(REQUIRE_NICKNAME_DUPLICATE_CHECK))
                    .when(signupService).saveMember(SESSION_ID, signupRequestOtherNickname);
            
            // when-then
            mvc.perform(post("/sign-up")
                            .content(mapper.writeValueAsString(signupRequestOtherNickname))
                            .contentType(APPLICATION_JSON)
                            .session(session))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("status").value(false))
                    .andExpect(jsonPath("message").value(REQUIRE_NICKNAME_DUPLICATE_CHECK))
                    .andDo(print());
        }
        
        @DisplayName("실패 - 비밀번호 공백")
        @Test
        void fail_password_empty() throws Exception {
            // given
            MockHttpSession session = new MockHttpSession();
            final String SESSION_ID = session.getId();
            
            SignupRequest signupRequest = SignupRequest.of(EMAIL, NICKNAME, PASSWORD);
            SignupRequest signupRequestPasswordException = SignupRequest.of(EMAIL, NICKNAME_CHANGED, "");
            
            doNothing().when(signupService).saveMember(SESSION_ID, signupRequest);
            doThrow(new SignUpException(VALID_PASSWORD_EXCEPTION_MESSAGE))
                    .when(signupService).saveMember(SESSION_ID, signupRequestPasswordException);
            
            // when-then
            mvc.perform(post("/sign-up")
                            .content(mapper.writeValueAsString(signupRequestPasswordException))
                            .contentType(APPLICATION_JSON)
                            .session(session))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("status").value(false))
                    .andExpect(jsonPath("message").value(VALID_EXCEPTION))
                    .andExpect(jsonPath("$.result.[0].field").value("password"))
                    .andExpect(jsonPath("$.result.[0].message").value(VALID_PASSWORD_EXCEPTION_MESSAGE))
                    .andDo(print());
        }
        
        @DisplayName("실패 - 비밀번호 4글자 미만")
        @Test
        void fail_password_too_short() throws Exception {
            // given
            MockHttpSession session = new MockHttpSession();
            final String SESSION_ID = session.getId();
            
            SignupRequest signupRequest = SignupRequest.of(EMAIL, NICKNAME, PASSWORD);
            SignupRequest signupRequestPasswordException = SignupRequest.of(EMAIL, NICKNAME_CHANGED, "tes");
            
            doNothing().when(signupService).saveMember(SESSION_ID, signupRequest);
            doThrow(new SignUpException(VALID_PASSWORD_EXCEPTION_MESSAGE))
                    .when(signupService).saveMember(SESSION_ID, signupRequestPasswordException);
            
            // when-then
            mvc.perform(post("/sign-up")
                            .content(mapper.writeValueAsString(signupRequestPasswordException))
                            .contentType(APPLICATION_JSON)
                            .session(session))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("status").value(false))
                    .andExpect(jsonPath("message").value(VALID_EXCEPTION))
                    .andExpect(jsonPath("$.result.[0].field").value("password"))
                    .andExpect(jsonPath("$.result.[0].message").value(VALID_PASSWORD_EXCEPTION_MESSAGE))
                    .andDo(print());
        }
        
        @DisplayName("성공")
        @Test
        void success() throws Exception {
            // given
            MockHttpSession session = new MockHttpSession();
            final String SESSION_ID = session.getId();

            SignupRequest signupRequest = SignupRequest.of(EMAIL, NICKNAME, PASSWORD);
            
            doNothing().when(signupService).saveMember(SESSION_ID, signupRequest);
            
            // when-then
            String responseMessage = String.format("%s님 가입을 축하합니다! 🥳", signupRequest.getNickname());
            mvc.perform(post("/sign-up")
                            .contentType(APPLICATION_JSON)
                            .content(mapper.writeValueAsString(signupRequest))
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("status").value(true))
                    .andExpect(jsonPath("message").value(responseMessage))
                    .andDo(print());
        }
    }
    
    @DisplayName("로그인 테스트")
    @Nested
    class LoginTest {
        
        private final RoleRepository roleRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        public LoginTest(@Autowired RoleRepository roleRepository,
                         @Autowired UserRepository userRepository,
                         @Autowired PasswordEncoder passwordEncoder) {
            this.roleRepository = roleRepository;
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
        }
        
        @DisplayName("실패 : 값이 안들어옴")
        @Test
        public void required_request_body() throws Exception {
            // when-then
            mvc.perform(post("/login"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(REQUIRED_REQUEST_BODY))
                    .andDo(print());
        }
        
        @DisplayName("실패 : 없는 아이디")
        @Test
        public void not_found_username() throws Exception {
            // given
            SignInRequest signInInfo = SignInRequest.of("1111@1234@4321", "aaaa");
            
            // when-then
            mvc.perform(post("/login")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(signInInfo)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(SIGNIN_FAIL))
                    .andDo(print());
        }
        
        @DisplayName("실패 : 잘못된 비밀번호")
        @Test
        public void password_match_fail() throws Exception {
            // given
            String email = "gimwlgus@daum.net";
            String nickname = "얼거스";
            String password = "1234";
            
            userRepository.save(User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .nickname(nickname)
                    .role(roleRepository.findByGrade(RoleType.MEMBER.name())).build());
            
            String passwordFault = "4321";
            SignInRequest signInInfo = SignInRequest.of(email, passwordFault);
            
            // when-then
            mvc.perform(post("/login")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(signInInfo)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value(SIGNIN_FAIL))
                    .andDo(print());
        }
        
        @DisplayName("성공")
        @Test
        public void success() throws Exception {
            
            // given
            String email = "gimwlgus@daum.net";
            String nickname = "얼거스";
            String password = "1234";
            
            userRepository.save(User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .nickname(nickname)
                    .role(roleRepository.findByGrade(RoleType.MEMBER.name())).build());
            
            SignInRequest signInInfo = SignInRequest.of(email, password);
            
            // when-then
            mvc.perform(post("/login")
                            .contentType(APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(signInInfo)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(String.format(SUCCESS_FORMAT_SIGN_IN, nickname, email)))
                    .andDo(print());
        }
    }
    
    @DisplayName("로그아웃 테스트")
    @Nested
    class LogoutTest {
        
        private final RoleRepository roleRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        public LogoutTest(@Autowired RoleRepository roleRepository,
                         @Autowired UserRepository userRepository,
                         @Autowired PasswordEncoder passwordEncoder) {
            this.roleRepository = roleRepository;
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
        }
        
        @DisplayName("성공")
        @Test
        public void success() throws Exception {
            // given
            String email = "gimwlgus@daum.net";
            String nickname = "얼거스";
            String password = "1234";
            
            userRepository.save(User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .nickname(nickname)
                    .role(roleRepository.findByGrade(RoleType.MEMBER.name())).build());
            
            SignInRequest signInInfo = SignInRequest.of(email, password);
            
            String jwt = String.format("%s%s",
                    JWT_PREFIX,
                    mvc.perform(post("/login")
                                    .contentType(APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(signInInfo)))
                            .andReturn()
                            .getResponse()
                            .getHeader(JWT_HEADER));
            
            // when-then
            mvc.perform(post("/logout").header(JWT_HEADER, jwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value(String.format(SUCCESS_FORMAT_SIGN_OUT, nickname, email)))
                    .andDo(print());
        }
    }
}