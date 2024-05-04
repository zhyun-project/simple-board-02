package kim.zhyun.serveruser.domain.signup.repository;

import kim.zhyun.serveruser.container.RedisTestContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("session user crud test")
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
class SessionUserRepositoryTest {
    
    @Autowired private SessionUserRepository sessionUserRepository;
    
    private final String sessionId = "session-id";
    private final String email = "email@email.email";
    private final boolean isEmailVerify = true;
    private final String nickname = "nickname";
    
    @BeforeEach public void saveInit() {
        sessionUserRepository.save(sessionUserBuilder(
                sessionId,
                email,
                isEmailVerify,
                nickname
        ));
    }
    
    @AfterEach public void deleteAll() {
        sessionUserRepository.deleteAll();
    }
    
    
    
    @DisplayName("저장 성공")
    @Test
    void success() {
        // given
        SessionUser sessionUser = sessionUserBuilder(
                sessionId,
                email,
                isEmailVerify,
                nickname
        );
        
        // when
        SessionUser saved = sessionUserRepository.save(sessionUser);
        
        // then
        assertThat(saved).isEqualTo(sessionUser);
    }
    
    
    @DisplayName("저장 실패 - null : session id")
    @Test
    void fail() {
        // given - when - then
        assertThrows(
                NullPointerException.class,
                () -> sessionUserRepository.save(sessionUserBuilder(
                        null,
                        email,
                        isEmailVerify,
                        nickname
                ))
        );
    }
    
    
    @DisplayName("읽기 성공")
    @Test
    void read_success() {
        // given - when
        SessionUser sessionUser = sessionUserRepository.findById(sessionId).get();
        
        // then
        assertAll(
                () -> assertEquals(sessionUser.getSessionId(), sessionId),
                () -> assertEquals(sessionUser.getEmail(), email),
                () -> assertEquals(sessionUser.getNickname(), nickname),
                () -> assertEquals(sessionUser.isEmailVerification(), isEmailVerify)
        );
    }
    

    @DisplayName("수정 성공")
    @Test
    void update_success() {
        // given
        SessionUser request = sessionUserRepository.findById(sessionId).get();
        
        request.setEmail("update@update.update");
        request.setEmailVerification(!request.isEmailVerification());
        request.setNickname("udt nickname");
        
        // when
        SessionUser before = sessionUserRepository.findById(sessionId).get();
        SessionUser after = sessionUserRepository.save(request);
        
        // then
        assertAll(
                () -> assertEquals(after.getSessionId(), before.getSessionId()),
                
                () -> assertNotEquals(after.getEmail(),             before.getEmail()),
                () -> assertNotEquals(after.getNickname(),          before.getNickname()),
                () -> assertNotEquals(after.isEmailVerification(),  before.isEmailVerification())
        );
        
    }
    
    
    @DisplayName("삭제 성공")
    @Test
    void delete_success() {
        // given
        SessionUser request = sessionUserRepository.findById(sessionId).get();
        
        // when
        sessionUserRepository.deleteById(request.getSessionId());
        
        // then
        assertThat(sessionUserRepository.findById(sessionId)).isEmpty();
    }
    
    
    
    private SessionUser sessionUserBuilder(
            String sessionId, String email, boolean isEmailVerify, String nickname
    ) {
        return SessionUser.builder()
                .sessionId(sessionId)
                .email(email)
                .emailVerification(isEmailVerify)
                .nickname(nickname)
                .build();
    }
}

