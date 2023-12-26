package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.data.NicknameDto;
import kim.zhyun.serveruser.entity.SessionUser;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.service.NicknameService;
import kim.zhyun.serveruser.service.SessionUserService;
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
@DisplayName("Redis Nickname Storage 테스트")
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
class NicknameStorageTest {
    private final String NICKNAME_RESERVED = "화이트그리숨엇슈";
    private final String NICKNAME_RESERVED_NOT = "그리숨엇슈🎁";
    private final String SESSION_ID_RESERVATION_PERSON = "6C62377C34168BB6DD496E8578447D78";
    private final String SESSION_ID_RESERVATION_PERSON_NOT = "4JK8377C34168BAS7G496E8578678GE2";

    private final NicknameService nicknameService;
    private final SessionUserService sessionUserService;
    private final RedisTemplate<String, String> redisTemplate;
    public NicknameStorageTest(@Autowired NicknameService nicknameService,
                               @Autowired SessionUserService sessionUserService,
                               @Autowired RedisTemplate<String, String> redisTemplate) {
        this.nicknameService = nicknameService;
        this.sessionUserService = sessionUserService;
        this.redisTemplate = redisTemplate;
    }
    
    @DisplayName("예약된 닉네임인지 검색 1")
    @Test
    void exist_by_nickname_1() {
        //given
        NicknameDto dto = NicknameDto.builder()
                .nickname(NICKNAME_RESERVED_NOT)
                .sessionId(SESSION_ID_RESERVATION_PERSON_NOT).build();
        
        // when
        boolean result1 = nicknameService.existNickname(dto);
        
        // then
        assertFalse(result1);
    }
    
    @DisplayName("예약된 닉네임인지 검색 2")
    @Test
    void exist_by_nickname_2() {
        //given
        NicknameDto dto = NicknameDto.builder()
                .nickname(NICKNAME_RESERVED)
                .sessionId(SESSION_ID_RESERVATION_PERSON_NOT).build();
        
        // when
        boolean result2 = nicknameService.existNickname(dto);
        
        // then
        assertTrue(result2);
    }
    
    @DisplayName("예약된 닉네임인지 검색 3")
    @Test
    void exist_by_nickname_3() {
        //given
        NicknameDto dto = NicknameDto.builder()
                .nickname(NICKNAME_RESERVED_NOT)
                .sessionId(SESSION_ID_RESERVATION_PERSON).build();
        
        // when
        boolean result3 = nicknameService.existNickname(dto);
        
        // then
        assertFalse(result3);
    }
    
    @DisplayName("예약된 닉네임인지 검색 4")
    @Test
    void exist_by_nickname_4() {
        //given
        NicknameDto dto = NicknameDto.builder()
                .nickname(NICKNAME_RESERVED)
                .sessionId(SESSION_ID_RESERVATION_PERSON).build();
        
        // when
        boolean result4 = nicknameService.existNickname(dto);
        
        // then
        assertFalse(result4);
    }
    
    @DisplayName("사용 가능한 닉네임인지 검색 - 불가능 도출")
    @Test
    void not_available_by_nickname() {
        //given
        NicknameDto dto = NicknameDto.builder()
                .nickname(NICKNAME_RESERVED)
                .sessionId(SESSION_ID_RESERVATION_PERSON_NOT).build();
        
        // when
        boolean result = nicknameService.availableNickname(dto);
        
        // then
        assertFalse(result);
    }
    
    @DisplayName("사용 가능한 닉네임인지 검색 - 가능 도출")
    @Test
    void available_by_nickname() {
        //given
        NicknameDto dto1 = NicknameDto.builder()
                .nickname(NICKNAME_RESERVED_NOT)
                .sessionId(SESSION_ID_RESERVATION_PERSON_NOT).build();
        NicknameDto dto2 = NicknameDto.builder()
                .nickname(NICKNAME_RESERVED_NOT)
                .sessionId(SESSION_ID_RESERVATION_PERSON).build();
        NicknameDto dto3 = NicknameDto.builder()
                .nickname(NICKNAME_RESERVED)
                .sessionId(SESSION_ID_RESERVATION_PERSON).build();
        
        // when
        boolean result1 = nicknameService.availableNickname(dto1);
        boolean result2 = nicknameService.availableNickname(dto2);
        boolean result3 = nicknameService.availableNickname(dto3);
        
        // then
        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
    }
    
    
    @DisplayName("닉네임 예약 목록에서 삭제 - 존재했던 닉네임")
    @Test
    void delete_by_nickname() {
        //given
        NicknameDto dto = NicknameDto.builder()
                .nickname(NICKNAME_RESERVED)
                .sessionId("").build();
        
        // when
        nicknameService.deleteNickname(dto);
        
        // then
        assertFalse(nicknameService.existNickname(dto));
    }
    
    @DisplayName("닉네임 예약 목록에서 삭제 - 없는 닉네임")
    @Test
    void delete_by_nickname_reserved_not() {
        //given
        NicknameDto dto = NicknameDto.builder()
                .nickname(NICKNAME_RESERVED)
                .sessionId("").build();
        
        // when
        nicknameService.deleteNickname(dto);
        
        // then
        assertFalse(nicknameService.existNickname(dto));
    }
    
    
    @BeforeEach
    void save_init_data() {
        NicknameDto dto = NicknameDto.builder()
                .nickname(NICKNAME_RESERVED)
                .sessionId(SESSION_ID_RESERVATION_PERSON).build();
        
        nicknameService.saveNickname(dto);
        sessionUserService.save(SessionUser.builder()
                .sessionId(SESSION_ID_RESERVATION_PERSON)
                .nickname(NICKNAME_RESERVED).build());
        print();
    }
    
    @AfterEach
    void print_after_log() {
        print();
        nicknameService.deleteNickname(NicknameDto.of(NICKNAME_RESERVED));
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