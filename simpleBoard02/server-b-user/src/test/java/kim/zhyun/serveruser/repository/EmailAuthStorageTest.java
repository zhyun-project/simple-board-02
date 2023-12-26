package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.data.EmailAuthDto;
import kim.zhyun.serveruser.entity.SessionUser;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import kim.zhyun.serveruser.service.EmailAuthService;
import kim.zhyun.serveruser.service.SessionUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("Redis Email Auth Storage 테스트")
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
class EmailAuthStorageTest {
    private final String SESSION_ID     = "6C62377C34168BB6DD496E8578447D78";
    private final String ID             = "gimwlgus@gmail.com";
    private final String CODE           = "email-auth-code";
    private final String CODE_NOT_EXIST = "email-auth-dfds";
    
    @Value("${sign-up.key.email}")
    private String KEY;
    
    private final SessionUserService sessionUserService;
    private final EmailAuthService emailAuthService;
    private final RedisTemplate<String, String> redisTemplate;
    public EmailAuthStorageTest(@Autowired SessionUserService sessionUserService,
                                @Autowired EmailAuthService emailAuthService,
                                @Autowired RedisTemplate<String, String> redisTemplate) {
        this.sessionUserService = sessionUserService;
        this.emailAuthService = emailAuthService;
        this.redisTemplate = redisTemplate;
    }

    
    @DisplayName("인증 코드 검증 - 만료됨")
    @Test
    void verify_code_expired() throws InterruptedException {
        // given
        EmailAuthDto dto = EmailAuthDto.builder()
                .email(ID).build();
        
        Thread.sleep(30_000);
        
        // when
        boolean existEmail = emailAuthService.existEmail(dto);
        
        // then
        assertFalse(existEmail);
    }
    
    @DisplayName("인증 코드 검증 - 유효 시도, 코드 불일치")
    @Test
    void verify_code_fail() {
        // given
        EmailAuthDto dto = EmailAuthDto.builder()
                .email(ID)
                .code(CODE_NOT_EXIST).build();
        
        // when
        boolean existEmail = emailAuthService.existEmail(dto);
        boolean existCode = emailAuthService.existCode(dto);
        
        // then
        assertTrue(existEmail);
        assertFalse(existCode);
    }
    
    @DisplayName("데이터 저장 - 만료 시간 : 30초 후")
    @Test
    void save() throws InterruptedException {
        // given
        EmailAuthDto dto = EmailAuthDto.builder()
                .email(ID)
                .code(CODE).build();
        
        // when
        emailAuthService.saveEmailAuthCode(dto);
        
        // then
        boolean existEmail = emailAuthService.existEmail(dto);
        assertTrue(existEmail);
        
        Thread.sleep(30_000);
        
        existEmail = emailAuthService.existEmail(dto);
        assertFalse(existEmail);
    }
    
    @DisplayName("인증 코드 검증 - 유효 시도, 코드 일치, 데이터 삭제")
    @Test
    void delete_by_id() {
        boolean emailVerificationOrigin = sessionUserService.findById(SESSION_ID).isEmailVerification();

        // given
        EmailAuthDto dto = EmailAuthDto.builder()
                .email(ID)
                .code(CODE).build();
        
        // when : 인증 성공
        emailAuthService.deleteAndUpdateSessionUserEmail(dto, SESSION_ID);
        
        // then
        boolean existEmail = emailAuthService.existEmail(dto);
        boolean emailVerification = sessionUserService.findById(SESSION_ID).isEmailVerification();

        assertFalse(existEmail);
        assertTrue(emailVerification);
        assertNotEquals(emailVerificationOrigin, emailVerification);
    }
    
    
    @BeforeEach
    void save_init_data() {
        // given
        SessionUser sessionUser = SessionUser.builder()
                .sessionId(SESSION_ID).build();
        
        EmailAuthDto dto = EmailAuthDto.builder()
                .email(ID)
                .code(CODE).build();
        
        sessionUserService.save(sessionUser);
        // 만료시간  : 30초
        emailAuthService.saveEmailAuthCode(dto);
        
        print();
    }
    
    @AfterEach
    void print_after_log() {
        print();
        emailAuthService.deleteAndUpdateSessionUserEmail(EmailAuthDto.builder()
                .email(ID).build(),
                SESSION_ID);
        sessionUserService.deleteById(SESSION_ID);
    }
    
    private void print() {
        log.info("");
        log.info("💁 All Data Logging ------------------------------------------------------------------------------------------------------------------------------------------------------------┐");
        redisTemplate.keys("*")
                .stream()
                .filter(key -> key.startsWith(KEY))
                .forEach(key -> redisTemplate.opsForSet().members(key)
                        .forEach(value -> log.info("EMAIL { key : {}, value : {} }", key, value)));
        log.info("{}", sessionUserService.findById(SESSION_ID));
        log.info("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------┘");
        log.info("");
    }
}
