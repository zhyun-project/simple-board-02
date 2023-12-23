package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.entity.EmailAuth;
import kim.zhyun.serveruser.repository.container.RedisTestContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@DisplayName("Redis Email Auth Storage 테스트")
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
class EmailAuthRedisRepositoryTest extends PrintLog<EmailAuthRedisRepository> {
    private final String ID   = "gimwlgus@gmail.com";
    private final String CODE = "email-auth-code";
    
    private final EmailAuthRedisRepository emailAuthRedisRepository;
    private final RedisTemplate<String, String> redisTemplate;
    public EmailAuthRedisRepositoryTest(@Autowired EmailAuthRedisRepository emailAuthRedisRepository,
                                        @Autowired RedisTemplate<String, String> redisTemplate) {
        super(emailAuthRedisRepository);
        this.emailAuthRedisRepository = emailAuthRedisRepository;
        this.redisTemplate = redisTemplate;
    }
    
    @DisplayName("전체 데이터 조회")
    @Test
    void find_all() { }
    
    @DisplayName("id: gimwlgus@gmail.com의 code 필드 조회")
    @Test
    void find_by_id() {
        // when
        Optional<EmailAuth> emailAuth = emailAuthRedisRepository.findById(ID);
        String code = emailAuth.get().getCode();
        
        // then
        Assertions.assertEquals(code, CODE);
    }
    
    @DisplayName("데이터 저장")
    @Test
    void save() {
        // when
        Optional<EmailAuth> emailAuth = emailAuthRedisRepository.findById(ID);
        
        // then
        assertTrue(emailAuth.isPresent());
    }
    
    @DisplayName("데이터 수정")
    @Test
    void update() {
        // when
        EmailAuth before = emailAuthRedisRepository.findById(ID).get();
        EmailAuth updateDto = EmailAuth.builder()
                .email(before.getEmail())
                .code(before.getCode())
                .expiredAt(before.getExpiredAt())
                .isVerification(!before.isVerification())
                .build();
        
        EmailAuth after = emailAuthRedisRepository.save(updateDto);
        
        // then
        assertFalse(before.isVerification());
        assertTrue(after.isVerification());
    }
    
    @DisplayName("데이터 삭제")
    @Test
    void delete() {
        // when
        emailAuthRedisRepository.deleteById(ID);
        
        // then
        assertThat(emailAuthRedisRepository.findById(ID)).isEmpty();
    }
    
    @DisplayName("Expired key 확인 - 키 생성 후 바로 조회 (만료시간 1분)")
    @Test
    void key_expired_before() {
        // when
        long keyExpire = EmailAuth.getKeyExpire(redisTemplate, ID);
        
        // then
        assertThat(keyExpire).isGreaterThanOrEqualTo(0);
        assertThat(keyExpire).isLessThan(1);
    }
    
    @DisplayName("Expired key 확인 - 1분 후 키 조회 (만료시간 1분)")
    @Test
    void key_expired_after() throws InterruptedException {
        // when
        Thread.sleep(60_000);
        log.info("now : {}", LocalDateTime.now());
        
        // then
        long keyExpire = EmailAuth.getKeyExpire(redisTemplate, ID);
        
        assertThat(keyExpire).isEqualTo(-2);
        assertFalse(repository.existsById(ID));
    }
    
    
    
    @BeforeEach
    public void inputData() {
        long ttl = 1;
        
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(ttl);
        EmailAuth given = EmailAuth.builder()
                .email(ID)
                .code(CODE)
                .expiredAt(expiredAt)
                .build();
        
        emailAuthRedisRepository.save(given);
        
        // 1분 후 key 삭제
        EmailAuth.setKeyExpire(redisTemplate, ID, ttl);
        long expire = EmailAuth.getKeyExpire(redisTemplate, ID);
        log.info("expired ==> {}", expire);
        log.info("expired ==> {}", LocalDateTime.now().plusMinutes(expire));
        
        emailAuthRedisRepository.findAll()
                .forEach(item -> log.info("saved init data ==> {}", item));
    }
    
}
