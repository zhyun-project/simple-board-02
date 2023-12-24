package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@DisplayName("Redis Nicname Storage 테스트")
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
class NicknameStorageTest {
    private final String NICKNAME_RESERVED = "화이트그리숨엇슈";
    private final String NICKNAME_RESERVED_NOT = "그리숨엇슈🎁";
    private final String SESSION_ID_RESERVATION_PERSON = "6C62377C34168BB6DD496E8578447D78";
    private final String SESSION_ID_RESERVATION_PERSON_NOT = "4JK8377C34168BAS7G496E8578678GE2";

    private final NicknameStorage nicknameStorage;
    private final RedisTemplate<String, String> redisTemplate;
    public NicknameStorageTest(@Autowired NicknameStorage nicknameStorage,
                               @Autowired RedisTemplate<String, String> redisTemplate) {
        this.nicknameStorage = nicknameStorage;
        this.redisTemplate = redisTemplate;
    }
    
    @DisplayName("예약된 닉네임인지 검색")
    @Test
    void exist_by_nickname() {
        // when
        boolean result1 = nicknameStorage.existNickname(NICKNAME_RESERVED_NOT, SESSION_ID_RESERVATION_PERSON_NOT);
        boolean result2 = nicknameStorage.existNickname(NICKNAME_RESERVED_NOT, SESSION_ID_RESERVATION_PERSON);
        
        // then
        assertFalse(result1);
        assertFalse(result2);
    }
    
    @DisplayName("사용 가능한 닉네임인지 검색 - 불가능 도출")
    @Test
    void not_available_by_nickname() {
        // when
        boolean result = nicknameStorage.availableNickname(NICKNAME_RESERVED, SESSION_ID_RESERVATION_PERSON_NOT);
        
        // then
        assertFalse(result);
    }
    
    @DisplayName("사용 가능한 닉네임인지 검색 - 가능 도출")
    @Test
    void available_by_nickname() {
        // when
        boolean result1 = nicknameStorage.availableNickname(NICKNAME_RESERVED_NOT, SESSION_ID_RESERVATION_PERSON_NOT);
        boolean result2 = nicknameStorage.availableNickname(NICKNAME_RESERVED_NOT, SESSION_ID_RESERVATION_PERSON);
        boolean result3 = nicknameStorage.availableNickname(NICKNAME_RESERVED, SESSION_ID_RESERVATION_PERSON);
        
        // then
        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
    }
    
    
    @DisplayName("닉네임 예약 목록에서 삭제 - 존재했던 닉네임")
    @Test
    void delete_by_nickname() {
        // when
        nicknameStorage.deleteNickname(NICKNAME_RESERVED);
        
        // then
        assertFalse(nicknameStorage.existNickname(NICKNAME_RESERVED, ""));
    }
    
    @DisplayName("닉네임 예약 목록에서 삭제 - 없는 닉네임")
    @Test
    void delete_by_nickname_reserved_not() {
        // when
        nicknameStorage.deleteNickname(NICKNAME_RESERVED_NOT);
        
        // then
        assertFalse(nicknameStorage.existNickname(NICKNAME_RESERVED_NOT, ""));
    }
    
    
    @BeforeEach
    void save_init_data() {
        nicknameStorage.saveNickname(NICKNAME_RESERVED, SESSION_ID_RESERVATION_PERSON);
        print();
    }
    
    @AfterEach
    void print_after_log() {
        print();
        nicknameStorage.deleteNickname(NICKNAME_RESERVED);
    }
    
    private void print() {
        log.info("");
        log.info("💁 All Data Logging ------------------------------------------------------------------------------------------------------------------------------------------------------------┐");
        redisTemplate.keys("*")
                .forEach(key -> redisTemplate.opsForSet().members(key)
                        .forEach(value -> log.info("NICKNAME { key : {}, value : {} }", key, value)));
        
        log.info("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------┘");
        log.info("");
    }
    
}