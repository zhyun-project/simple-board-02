package kim.zhyun.serveruser.domain.signup.repository;

import kim.zhyun.serveruser.container.RedisTestContainer;
import org.junit.jupiter.api.*;
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
    
    
    
    @DisplayName("저장 테스트")
    @Nested
    class Save {
        
        @DisplayName("성공")
        @Test
        void success() {
            // given
            SessionUser sessionUser = sessionUserBuilder(
                    "session-id",
                    "email@email.email",
                    true,
                    "nickname"
            );
            
            // when
            SessionUser saved = sessionUserRepository.save(sessionUser);
            
            // then
            assertThat(saved).isEqualTo(sessionUser);
        }
        
        @DisplayName("실패 - session id = null")
        @Test
        void fail() {
            // given - when - then
            assertThrows(
                    NullPointerException.class,
                    () -> sessionUserRepository.save(sessionUserBuilder(
                            null,
                            "email@email.email",
                            true,
                            "nickname"
                    ))
            );
        }
        
    }
    
    
    @DisplayName("읽기 테스트")
    @Nested
    class Read {
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
        
        @DisplayName("성공")
        @Test
        void success() {
            // given - when
            SessionUser sessionUser = sessionUserRepository.findById(sessionId).get();
            
            // then
            assertThat(sessionUser.getSessionId())         .isEqualTo(sessionId);
            assertThat(sessionUser.getEmail())             .isEqualTo(email);
            assertThat(sessionUser.isEmailVerification())  .isEqualTo(isEmailVerify);
            assertThat(sessionUser.getNickname())          .isEqualTo(nickname);
        }
    }
    

    @DisplayName("수정 테스트")
    @Nested
    class Update {
        private String sessionId = "session-id";
        
        @BeforeEach public void saveInit() {
            sessionUserRepository.save(sessionUserBuilder(
                    sessionId,
                    "email@email.email",
                    true,
                    "nickname"
            ));
        }
        
        @AfterEach public void deleteAll() {
            sessionUserRepository.deleteAll();
        }
        
        @DisplayName("성공")
        @Test
        void success() {
            // given
            SessionUser request = sessionUserRepository.findById(sessionId).get();
            
            request.setEmail("update@update.update");
            request.setEmailVerification(!request.isEmailVerification());
            request.setNickname("udt nickname");
            
            // when
            SessionUser before = sessionUserRepository.findById(sessionId).get();
            SessionUser after = sessionUserRepository.save(request);
            
            // then
            assertThat(after.getSessionId())         .isEqualTo(before.getSessionId());
            assertThat(after.getEmail())             .isNotEqualTo(before.getEmail());
            assertThat(after.isEmailVerification())  .isNotEqualTo(before.isEmailVerification());
            assertThat(after.getNickname())          .isNotEqualTo(before.getNickname());
        }
    }
    
    
    @DisplayName("삭제 테스트")
    @Nested
    class Delete {
        private String sessionId = "session-id";
        
        @BeforeEach public void saveInit() {
            sessionUserRepository.save(sessionUserBuilder(
                    sessionId,
                    "email@email.email",
                    true,
                    "nickname"
            ));
        }
        
        @AfterEach public void deleteAll() {
            sessionUserRepository.deleteAll();
        }
        
        @DisplayName("성공")
        @Test
        void success() {
            // given
            SessionUser request = sessionUserRepository.findById(sessionId).get();
            
            // when
            sessionUserRepository.deleteById(request.getSessionId());
            
            // then
            assertThat(sessionUserRepository.findById(sessionId)).isEmpty();
        }
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