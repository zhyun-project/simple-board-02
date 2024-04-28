package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.domain.signup.controller.model.dto.NicknameFindDto;
import kim.zhyun.serveruser.domain.signup.repository.SessionUser;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.domain.signup.service.NicknameReserveService;
import kim.zhyun.serveruser.domain.member.service.SessionUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@Order(0)
@DisplayName("Redis Nickname Storage 테스트")
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
class NicknameStorageTest {
    private final String NICKNAME_RESERVED = "화이트그리숨엇슈";
    private final String NICKNAME_RESERVED_NOT = "그리숨엇슈🎁";
    private final String SESSION_ID_RESERVATION_PERSON = "6C62377C34168BB6DD496E8578447D78";
    private final String SESSION_ID_RESERVATION_PERSON_NOT = "4JK8377C34168BAS7G496E8578678GE2";

    private final NicknameReserveService nicknameReserveService;
    private final SessionUserService sessionUserService;
    private final RedisTemplate<String, String> redisTemplate;
    public NicknameStorageTest(@Autowired NicknameReserveService nicknameReserveService,
                               @Autowired SessionUserService sessionUserService,
                               @Autowired RedisTemplate<String, String> redisTemplate) {
        this.nicknameReserveService = nicknameReserveService;
        this.sessionUserService = sessionUserService;
        this.redisTemplate = redisTemplate;
    }
    
    
    @DisplayName("사용 가능한 닉네임인지 검색 - false : 다른 사람이 선점한 닉네임")
    @Test
    void not_available_by_nickname() {
        //given
        NicknameFindDto dto = NicknameFindDto.builder()
                .nickname(NICKNAME_RESERVED)
                .sessionId(SESSION_ID_RESERVATION_PERSON_NOT).build();
        
        // when
        boolean result = nicknameReserveService.availableNickname(dto);
        
        // then
        assertFalse(result);
    }
    
    @DisplayName("사용 가능한 닉네임인지 검색 - true : 예약 안 된 닉네임")
    @Test
    void available_nickname() {
        //given
        NicknameFindDto dto = NicknameFindDto.builder()
                .nickname(NICKNAME_RESERVED_NOT)
                .sessionId(SESSION_ID_RESERVATION_PERSON_NOT).build();
        
        // when
        boolean result = nicknameReserveService.availableNickname(dto);
        
        // then
        assertTrue(result);
    }
    
    @DisplayName("사용 가능한 닉네임인지 검색 - true : 내가 선점한 닉네임")
    @Test
    void available_nickname_its_mine() {
        //given
        NicknameFindDto dto = NicknameFindDto.builder()
                .nickname(NICKNAME_RESERVED)
                .sessionId(SESSION_ID_RESERVATION_PERSON).build();
        
        // when
        boolean result = nicknameReserveService.availableNickname(dto);
        
        // then
        assertTrue(result);
    }
    
    @DisplayName("사용 가능한 닉네임인지 검색 - true : 닉네임(A)를 선점한 유저(AA)가 닉네임(B)를 조회했을 때 유저(ZZ)가 닉네임(A) 조회")
    @Test
    void available_by_nickname_reserve_canceled() {
        //given
        NicknameFindDto dto1 = NicknameFindDto.builder()
                .nickname(NICKNAME_RESERVED)
                .sessionId(SESSION_ID_RESERVATION_PERSON_NOT).build();
        NicknameFindDto dto2 = NicknameFindDto.builder()
                .nickname(NICKNAME_RESERVED_NOT)
                .sessionId(SESSION_ID_RESERVATION_PERSON).build();
        
        // when
        boolean targetBefore = nicknameReserveService.availableNickname(dto1);
        boolean target = nicknameReserveService.availableNickname(dto2);
        boolean targetAfter = nicknameReserveService.availableNickname(dto1);
        
        // then
        assertFalse(targetBefore);
        assertTrue(target);
        assertTrue(targetAfter);
    }
    
    
    @DisplayName("닉네임 예약 목록에서 삭제 - 존재했던 닉네임")
    @Test
    void delete_by_nickname() {
        //given
        NicknameFindDto dto = NicknameFindDto.of(NICKNAME_RESERVED);
        
        // when
        nicknameReserveService.deleteNickname(dto);
        
        // then
        assertFalse(existNickname(dto));
    }
    
    
    @DisplayName("닉네임 예약 목록에서 삭제 - 없는 닉네임")
    @Test
    void delete_by_nickname_reserved_not() {
        //given
        NicknameFindDto dto = NicknameFindDto.of("asdasdasdasd");
        
        // when
        nicknameReserveService.deleteNickname(dto);
        
        // then
        assertFalse(existNickname(dto));
    }
    
    
    private Boolean existNickname(NicknameFindDto dto) {
        return redisTemplate.hasKey(dto.getNickname());
    }
    
    @BeforeEach
    void save_init_data() {
        NicknameFindDto dto = NicknameFindDto.builder()
                .nickname(NICKNAME_RESERVED)
                .sessionId(SESSION_ID_RESERVATION_PERSON).build();
        
        nicknameReserveService.saveNickname(dto);
        sessionUserService.save(SessionUser.builder()
                .sessionId(SESSION_ID_RESERVATION_PERSON)
                .nickname(NICKNAME_RESERVED).build());
        print();
    }
    
    @AfterEach
    void print_after_log() {
        print();
        nicknameReserveService.deleteNickname(NicknameFindDto.of(NICKNAME_RESERVED));
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