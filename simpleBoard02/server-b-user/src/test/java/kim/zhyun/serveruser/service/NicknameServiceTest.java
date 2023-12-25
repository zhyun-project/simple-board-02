package kim.zhyun.serveruser.service;

import kim.zhyun.serveruser.entity.SessionUser;
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
class NicknameServiceTest {
    private final String NICKNAME_RESERVED = "화이트그리숨엇슈";
    private final String NICKNAME_RESERVED_NOT = "그리숨엇슈🎁";
    private final String SESSION_ID_RESERVATION_PERSON = "6C62377C34168BB6DD496E8578447D78";
    private final String SESSION_ID_RESERVATION_PERSON_NOT = "4JK8377C34168BAS7G496E8578678GE2";

    private final NicknameService nicknameService;
    private final SessionUserService sessionUserService;
    private final RedisTemplate<String, String> redisTemplate;
    public NicknameServiceTest(@Autowired NicknameService nicknameService,
                               @Autowired SessionUserService sessionUserService,
                               @Autowired RedisTemplate<String, String> redisTemplate) {
        this.nicknameService = nicknameService;
        this.sessionUserService = sessionUserService;
        this.redisTemplate = redisTemplate;
    }
    
    @DisplayName("예약된 닉네임인지 검색 1")
    @Test
    void exist_by_nickname_1() {
        // when
        boolean result1 = nicknameService.existNickname(NICKNAME_RESERVED_NOT, SESSION_ID_RESERVATION_PERSON_NOT);
        
        // then
        assertFalse(result1);
    }
    
    @DisplayName("예약된 닉네임인지 검색 2")
    @Test
    void exist_by_nickname_2() {
        // when
        boolean result2 = nicknameService.existNickname(NICKNAME_RESERVED, SESSION_ID_RESERVATION_PERSON_NOT);
        
        // then
        assertTrue(result2);
    }
    
    @DisplayName("예약된 닉네임인지 검색 3")
    @Test
    void exist_by_nickname_3() {
        // when
        boolean result3 = nicknameService.existNickname(NICKNAME_RESERVED_NOT, SESSION_ID_RESERVATION_PERSON);
        
        // then
        assertFalse(result3);
    }
    
    @DisplayName("예약된 닉네임인지 검색 4")
    @Test
    void exist_by_nickname_4() {
        // when
        boolean result4 = nicknameService.existNickname(NICKNAME_RESERVED, SESSION_ID_RESERVATION_PERSON);
        
        // then
        assertFalse(result4);
    }
    
    @DisplayName("사용 가능한 닉네임인지 검색 - 불가능 도출")
    @Test
    void not_available_by_nickname() {
        // when
        boolean result = nicknameService.availableNickname(NICKNAME_RESERVED, SESSION_ID_RESERVATION_PERSON_NOT);
        
        // then
        assertFalse(result);
    }
    
    @DisplayName("사용 가능한 닉네임인지 검색 - 가능 도출")
    @Test
    void available_by_nickname() {
        // when
        boolean result1 = nicknameService.availableNickname(NICKNAME_RESERVED_NOT, SESSION_ID_RESERVATION_PERSON_NOT);
        boolean result2 = nicknameService.availableNickname(NICKNAME_RESERVED_NOT, SESSION_ID_RESERVATION_PERSON);
        boolean result3 = nicknameService.availableNickname(NICKNAME_RESERVED, SESSION_ID_RESERVATION_PERSON);
        
        // then
        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
    }
    
    
    @DisplayName("닉네임 예약 목록에서 삭제 - 존재했던 닉네임")
    @Test
    void delete_by_nickname() {
        // when
        nicknameService.deleteNickname(NICKNAME_RESERVED);
        
        // then
        assertFalse(nicknameService.existNickname(NICKNAME_RESERVED, ""));
    }
    
    @DisplayName("닉네임 예약 목록에서 삭제 - 없는 닉네임")
    @Test
    void delete_by_nickname_reserved_not() {
        // when
        nicknameService.deleteNickname(NICKNAME_RESERVED_NOT);
        
        // then
        assertFalse(nicknameService.existNickname(NICKNAME_RESERVED_NOT, ""));
    }
    
    
    @BeforeEach
    void save_init_data() {
        nicknameService.saveNickname(NICKNAME_RESERVED, SESSION_ID_RESERVATION_PERSON);
        sessionUserService.save(SessionUser.builder()
                .sessionId(SESSION_ID_RESERVATION_PERSON)
                .nickname(NICKNAME_RESERVED).build());
        print();
    }
    
    @AfterEach
    void print_after_log() {
        print();
        nicknameService.deleteNickname(NICKNAME_RESERVED);
    }
    
    private void print() {
        log.info("");
        log.info("💁 All Data Logging ------------------------------------------------------------------------------------------------------------------------------------------------------------┐");
        redisTemplate.keys("*")
                .stream()
                .filter(key -> !key.startsWith("SESSION_ID") && !key.startsWith("EMAIL"))
                .forEach(key -> redisTemplate.opsForSet().members(key)
                        .forEach(value -> log.info("NICKNAME { key : {}, value : {} }", key, value)));
        
        log.info("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------┘");
        log.info("");
    }
    
}