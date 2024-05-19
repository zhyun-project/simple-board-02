package kim.zhyun.serveruser.domain.member.service;

import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.serveruser.common.value.SessionUserValue;
import kim.zhyun.serveruser.domain.signup.business.model.SessionUserEmailUpdateDto;
import kim.zhyun.serveruser.domain.signup.controller.model.dto.NicknameUpdateDto;
import kim.zhyun.serveruser.domain.signup.repository.SessionUser;
import kim.zhyun.serveruser.domain.signup.repository.SessionUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_REQUIRE_MAIL_DUPLICATE_CHECK;
import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("session user service")
@ExtendWith(MockitoExtension.class)
class SessionUserServiceTest {
    
    SessionUserService sessionUserService;
    
    @Mock SessionUserRepository sessionUserRepository;
    @Mock RedisTemplate<String, String> redisTemplate;
    
    int sessionExpireTime = 30;
    

    @BeforeEach
    void SessionUserServiceCreate() {
        MockitoAnnotations.openMocks(this);
        sessionUserService = new SessionUserService(
                sessionUserRepository,
                redisTemplate,
                new SessionUserValue(
                        "SESSION_ID:",
                        "EMAIL:",
                        "NICKNAME:",
                        sessionExpireTime
                )
        );
    }
    
    @DisplayName("nickname 중복 확인 여부 - true")
    @Test
    void existNicknameDuplicateCheckWithThrow() {
        String sessionId = "session-id";
        String requestNickname = "update-nickname";
        String checkedNickname = "update-nickname";
        
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(sessionId)
                .email("gimwlgus@email.mail")
                .emailVerification(true)
                .nickname(checkedNickname)
                .build();
        
        given(sessionUserRepository.findById(sessionId)).willReturn(Optional.of(sessionUser));
        
        
        // when
        boolean result = sessionUserService.existNicknameDuplicateCheckWithThrow(sessionId, requestNickname);
        
        
        // then
        assertTrue(result);
    }
    
    @DisplayName("nickname 중복 확인 여부 - false")
    @ParameterizedTest(name = "checked, request = {0}, update-nickname")
    @ValueSource(strings = "nickname")
    @NullSource
    void existNicknameDuplicateCheckWithThrow(String checkedNickname) {
        String sessionId = "session-id";
        String requestNickname = "update-nickname";
        
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(sessionId)
                .email("gimwlgus@email.mail")
                .emailVerification(true)
                .nickname(checkedNickname)
                .build();
        
        given(sessionUserRepository.findById(sessionId)).willReturn(Optional.of(sessionUser));
        
        // when-then
        assertThrows(
                ApiException.class,
                () -> sessionUserService.existNicknameDuplicateCheckWithThrow(sessionId, requestNickname),
                EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK
        );
    }
    
    
    @DisplayName("email 중복 확인 여부 - true")
    @Test
    void emailDuplicateCheckWithThrow() {
        String sessionId = "session-id";
        String requestEmail = "update-email@mail.ail";
        String checkedEmail = "update-email@mail.ail";
        
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(sessionId)
                .email(checkedEmail)
                .emailVerification(true)
                .nickname("nickname")
                .build();
        
        given(sessionUserRepository.findById(sessionId)).willReturn(Optional.of(sessionUser));
        
        
        // when-then
        assertDoesNotThrow(
                () -> sessionUserService.emailDuplicateCheckWithThrow(sessionId, requestEmail)
        );
    }
    
    @DisplayName("email 중복 확인 여부 - false")
    @ParameterizedTest(name = "checked, request = {0}, gimwlgus@gmail.com")
    @ValueSource(strings = "gimwlgus@naver.com")
    @NullSource
    void emailDuplicateCheckWithThrow(String checkedEmail) {
        String sessionId = "session-id";
        String requestEmail = "gimwlgus@gmail.com";
        
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(sessionId)
                .email(checkedEmail)
                .emailVerification(true)
                .nickname("nickname")
                .build();
        
        given(sessionUserRepository.findById(sessionId)).willReturn(Optional.of(sessionUser));
        
        
        // when-then
        assertThrows(
                ApiException.class,
                () -> sessionUserService.emailDuplicateCheckWithThrow(sessionId, requestEmail),
                EXCEPTION_REQUIRE_MAIL_DUPLICATE_CHECK
        );
    }
    
    
    @DisplayName("session user 정보 조회 - 있음")
    @Test
    void findById_true() {
        String sessionId = "session-id";
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(sessionId)
                .build();
        
        given(sessionUserRepository.findById(sessionId)).willReturn(Optional.of(sessionUser));
        
        // when
        sessionUserService.findById(sessionId);
        
        // then
        then(sessionUserRepository).should(times(1)).findById(sessionId);
        then(sessionUserRepository).should(times(0)).save(sessionUser);
    }
    
    @DisplayName("session user 정보 조회 - 없음: 신규 생성 후 조회")
    @Test
    void findById_false() {
        String sessionId = "session-id";
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(sessionId)
                .build();
        
        given(sessionUserRepository.findById(sessionId)).willReturn(Optional.empty());
        given(sessionUserRepository.save(sessionUser)).willReturn(sessionUser);
        
        // when
        sessionUserService.findById(sessionId);
        
        // then
        then(sessionUserRepository).should(times(1)).findById(sessionId);
        then(sessionUserRepository).should(times(1)).save(sessionUser);
    }
    
    
    @DisplayName("sessoin user 유무 조회")
    @ParameterizedTest
    @ValueSource(booleans = {
            true, false
    })
    void existsById(boolean exist) {
        String sessionId = "session-id";
        
        given(sessionUserRepository.existsById(sessionId)).willReturn(exist);
        
        // when
        boolean result = sessionUserService.existsById(sessionId);
        
        // then
        assertEquals(exist, result);
    }
    
    @DisplayName("session user 저장 - false: session id = null")
    @Test
    void save_fail() {
        // given-when-then
        assertThrows(
                NullPointerException.class,
                () -> SessionUser.builder()
                        .sessionId(null)
                        .nickname("nickname")
                        .email("email@mail.ail")
                        .emailVerification(true)
                        .build()
        );
    }
    
    @DisplayName("session user 저장 - success")
    @Test
    void save_success() {
        // given
        SessionUser sessionUser = SessionUser.builder()
                .sessionId("session-id")
                .nickname("nickname")
                .email("email@mail.ail")
                .emailVerification(true)
                .build();
        given(sessionUserRepository.save(sessionUser)).willReturn(sessionUser);
        
        // when-then
        assertDoesNotThrow(
                () -> sessionUserService.save(sessionUser)
        );
    }
    
    @DisplayName("이메일 수정")
    @Test
    void updateEmail() {
        // given
        SessionUserEmailUpdateDto sessionUserEmailUpdateDto = SessionUserEmailUpdateDto.builder()
                .id("session-id")
                .email("email@mail.ail")
                .emailVerification(true)
                .build();
        
        // -- find session user by id
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(sessionUserEmailUpdateDto.getId())
                .build();
        given(sessionUserRepository.findById(sessionUserEmailUpdateDto.getId())).willReturn(Optional.of(sessionUser));
        
        // -- session user update - email 수정
        sessionUser.setEmail(sessionUserEmailUpdateDto.getEmail().replace("EMAIL:", ""));
        sessionUser.setEmailVerification(sessionUserEmailUpdateDto.isEmailVerification());
        
        given(sessionUserRepository.save(sessionUser)).willReturn(sessionUser);
        
        
        // when
        sessionUserService.updateEmail(sessionUserEmailUpdateDto);
        
        
        // then
        then(sessionUserRepository).should(times(1)).findById(sessionUserEmailUpdateDto.getId());
        then(sessionUserRepository).should(times(1)).save(sessionUser);
    }
    
    @DisplayName("session user - nickname 갱신")
    @Test
    void updateNickname() {
        NicknameUpdateDto nicknameUpdateDto = NicknameUpdateDto.builder()
                .id("session-id")
                .nickname("update-nickname")
                .build();
        
        // -- find session user by id
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(nicknameUpdateDto.getId())
                .build();
        given(sessionUserRepository.findById(nicknameUpdateDto.getId())).willReturn(Optional.of(sessionUser));
        
        // -- session user update - nickname 수정
        sessionUser.setNickname(nicknameUpdateDto.getNickname().replace("NICKNAME:", ""));
        
        given(sessionUserRepository.save(sessionUser)).willReturn(sessionUser);
        
        
        // when
        sessionUserService.updateNickname(nicknameUpdateDto);
        
        
        // then
        then(sessionUserRepository).should(times(1)).findById(nicknameUpdateDto.getId());
        then(sessionUserRepository).should(times(1)).save(sessionUser);
        
    }
    
    @DisplayName("session user 삭제")
    @Test
    void deleteById() {
        String id = "session-id";
        willDoNothing().given(sessionUserRepository).deleteById(id);
        
        // when
        sessionUserService.deleteById(id);
        
        // then
        then(sessionUserRepository).should(times(1)).deleteById(id);
    }
    
    @DisplayName("session user expire time 초기화 - nickname 중복확인 한 경우")
    @Test
    void initSessionUserExpireTime() {
        String id = "session-id";
        
        // -- find session user by id
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(id)
                .nickname("nickname")
                .email("gimwlgus@email.mail")
                .emailVerification(true)
                .build();
        given(sessionUserRepository.findById(id)).willReturn(Optional.of(sessionUser));
        
        given(redisTemplate.expire("NICKNAME:" + sessionUser.getNickname(), sessionExpireTime, TimeUnit.MINUTES)).willReturn(true);
        given(redisTemplate.expire("SESSION_ID:" + sessionUser.getSessionId(), sessionExpireTime, TimeUnit.MINUTES)).willReturn(true);
        
        
        // when
        sessionUserService.initSessionUserExpireTime(id);
        
        
        // then
        then(sessionUserRepository).should(times(1)).findById(id);
        then(redisTemplate).should(times(1)).expire("NICKNAME:" + sessionUser.getNickname(), sessionExpireTime, TimeUnit.MINUTES);
        then(redisTemplate).should(times(1)).expire("SESSION_ID:" + sessionUser.getSessionId(), sessionExpireTime, TimeUnit.MINUTES);
    }
    
    
    @DisplayName("session user expire time 초기화 - nickname 중복확인 하지 않은 경우")
    @Test
    void initSessionUserExpireTime_not_duplicate_nickname() {
        String id = "session-id";
        
        // -- find session user by id
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(id)
                .nickname(null)
                .email("gimwlgus@email.mail")
                .emailVerification(true)
                .build();
        given(sessionUserRepository.findById(id)).willReturn(Optional.of(sessionUser));
        
        given(redisTemplate.expire("SESSION_ID:" + sessionUser.getSessionId(), sessionExpireTime, TimeUnit.MINUTES)).willReturn(true);
        
        
        // when
        sessionUserService.initSessionUserExpireTime(id);
        
        
        // then
        then(sessionUserRepository).should(times(1)).findById(id);
        then(redisTemplate).should(never()).expire("NICKNAME:" + sessionUser.getNickname(), sessionExpireTime, TimeUnit.MINUTES);
        then(redisTemplate).should(times(1)).expire("SESSION_ID:" + sessionUser.getSessionId(), sessionExpireTime, TimeUnit.MINUTES);
    }
    
}
