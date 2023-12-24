package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.entity.SessionUser;
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
class SessionUserRedisRepositoryTest extends PrintLog<SessionUserRedisRepository>{
    private final String ID = "6C62377C34168BB6DD496E8578447D78";
    
    private final SessionUserRedisRepository sessionUserRedisRepository;
    public SessionUserRedisRepositoryTest(@Autowired SessionUserRedisRepository sessionUserRedisRepository) {
        super(sessionUserRedisRepository);
        this.sessionUserRedisRepository = sessionUserRedisRepository;
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
        Optional<SessionUser> optionalSessionId = sessionUserRedisRepository.findById(ID);
        SessionUser sessionUser = optionalSessionId.get();
        sessionUser.setEmail(email);
        sessionUserRedisRepository.save(sessionUser);
        
        // then
        SessionUser result = sessionUserRedisRepository.findById(ID).get();
        assertEquals(result.getEmail(), email);
        assertEquals(result.getSessionId(), ID);
    }
    
    
    @DisplayName("nickname 추가")
    @Test
    void update_nickname() {
        // given
        String nickname = "얼거스";
        
        // when
        Optional<SessionUser> optionalSessionId = sessionUserRedisRepository.findById(ID);
        SessionUser sessionUser = optionalSessionId.get();
        sessionUser.setNickname(nickname);
        sessionUserRedisRepository.save(sessionUser);
        
        // then
        SessionUser result = sessionUserRedisRepository.findById(ID).get();
        assertEquals(result.getNickname(), nickname);
        assertEquals(result.getSessionId(), ID);
    }
    
    @DisplayName("session id 삭제")
    @Test
    void delete() {
        // when
        assertFalse(sessionUserRedisRepository.findById(ID).isEmpty());
        sessionUserRedisRepository.deleteById(ID);
        
        // then
        assertTrue(sessionUserRedisRepository.findById(ID).isEmpty());
    }
    
    @DisplayName("session id 조회 - true")
    @Test
    void find_by_id() {
        // when
        assertTrue(sessionUserRedisRepository.existsById(ID));
    }
    
    
    @BeforeEach
    public void inputData() {
        SessionUser given = SessionUser.builder()
                .sessionId(ID)
                .build();
        
        sessionUserRedisRepository.save(given);
        
        sessionUserRedisRepository.findAll()
                .forEach(item -> log.info("saved init data ==> {}", item));
    }
}