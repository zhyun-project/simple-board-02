package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.data.entity.SessionUser;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("Redis Session Id Storage 테스트")
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
class SessionUserRepositoryTest extends PrintLog<SessionUserRepository>{
    private final String ID = "6C62377C34168BB6DD496E8578447D78";
    
    private final SessionUserRepository sessionUserRepository;
    public SessionUserRepositoryTest(@Autowired SessionUserRepository sessionUserRepository) {
        super(sessionUserRepository);
        this.sessionUserRepository = sessionUserRepository;
    }
    
    @DisplayName("전체 데이터 조회")
    @Test
    void find_all() { }
    
    @DisplayName("email 추가")
    @Test
    void update_email() {
        // given
        String email = "gimwlgus@gmail.com";
        
        // when
        Optional<SessionUser> optionalSessionId = sessionUserRepository.findById(ID);
        SessionUser sessionUser = optionalSessionId.get();
        sessionUser.setEmail(email);
        sessionUserRepository.save(sessionUser);
        
        // then
        SessionUser result = sessionUserRepository.findById(ID).get();
        assertEquals(result.getEmail(), email);
        assertEquals(result.getSessionId(), ID);
    }
    
    
    @DisplayName("email verification true 업데이트")
    @Test
    void update_email_verification() {
        // given
        boolean updateEmailVerificatoin = true;
        
        // when
        Optional<SessionUser> optionalSessionId = sessionUserRepository.findById(ID);
        SessionUser sessionUser = optionalSessionId.get();
        
        boolean beforeEmailVerification = sessionUser.isEmailVerification();
        
        sessionUser.setEmailVerification(updateEmailVerificatoin);
        sessionUserRepository.save(sessionUser);
        
        // then
        SessionUser result = sessionUserRepository.findById(ID).get();
        assertNotEquals(result.isEmailVerification(), beforeEmailVerification);
        assertEquals(result.isEmailVerification(), updateEmailVerificatoin);
    }
    
    
    @DisplayName("nickname 추가")
    @Test
    void update_nickname() {
        // given
        String nickname = "얼거스";
        
        // when
        Optional<SessionUser> optionalSessionId = sessionUserRepository.findById(ID);
        SessionUser sessionUser = optionalSessionId.get();
        sessionUser.setNickname(nickname);
        sessionUserRepository.save(sessionUser);
        
        // then
        SessionUser result = sessionUserRepository.findById(ID).get();
        assertEquals(result.getNickname(), nickname);
        assertEquals(result.getSessionId(), ID);
    }
    
    @DisplayName("session id 삭제")
    @Test
    void delete() {
        // when
        assertFalse(sessionUserRepository.findById(ID).isEmpty());
        sessionUserRepository.deleteById(ID);
        
        // then
        assertTrue(sessionUserRepository.findById(ID).isEmpty());
    }
    
    @DisplayName("session id 조회 - true")
    @Test
    void find_by_id() {
        // when
        assertTrue(sessionUserRepository.existsById(ID));
    }
    
    
    @BeforeEach
    public void inputData() {
        SessionUser given = SessionUser.builder()
                .sessionId(ID)
                .build();
        
        sessionUserRepository.save(given);
        
        sessionUserRepository.findAll()
                .forEach(item -> log.info("saved init data ==> {}", item));
    }
}