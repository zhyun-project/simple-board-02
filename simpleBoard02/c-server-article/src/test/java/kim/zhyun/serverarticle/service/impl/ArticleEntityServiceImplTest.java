package kim.zhyun.serverarticle.service.impl;

import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.jwt.domain.repository.JwtUserInfoRepository;
import kim.zhyun.serverarticle.container.RedisTestContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static kim.zhyun.jwt.common.constants.type.RoleType.TYPE_ADMIN;
import static kim.zhyun.jwt.common.constants.type.RoleType.TYPE_MEMBER;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Order(0)
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
class ArticleEntityServiceImplTest {
    private final JwtUserInfoRepository jwtUserInfoRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${key.redis.articleId}") private String REDIS_ARTICLE_ID_KEY;
    
    public ArticleEntityServiceImplTest(@Autowired JwtUserInfoRepository jwtUserInfoRepository,
                                        @Autowired RedisTemplate<String, String> redisTemplate) {
        this.jwtUserInfoRepository = jwtUserInfoRepository;
        this.redisTemplate = redisTemplate;
    }
    
    @DisplayName("redis에서 유저 정보 조회")
    @Test
    void user_info_from_redis() {
        Map<Long, JwtUserInfoDto> jwtUserMap = new HashMap<>();
        jwtUserInfoRepository.findAll()
                .forEach(jwtUserInfo -> jwtUserMap.put(jwtUserInfo.getId(), JwtUserInfoDto.from(jwtUserInfo)));
        
        jwtUserMap.forEach((id, dto) -> log.info("user id : {}, user dto : {}", id, dto));
    }
    
    @DisplayName("getJwtUserDto() 검증 - 존재하는 아이디")
    @Test
    void user_info_from_redis_by_user_id_is_exist_true() {
        JwtUserInfoDto jwtUserInfoDto = JwtUserInfoDto.from(jwtUserInfoRepository.findById(2L).get());
        
        assertThat(jwtUserInfoDto.getId()).isEqualTo(2L);
        log.info("user info : {}", jwtUserInfoDto);
    }
    
    @DisplayName("getJwtUserDto() 검증 - 없는 아이디")
    @Test
    void user_info_from_redis_by_user_id_is_exist_false() {
        Optional<JwtUserInfoEntity> container = jwtUserInfoRepository.findById(100L);
        
        assertThat(container).isEmpty();
    }
    
    @DisplayName("getNewArticleId() 검증 - 신규 가입자")
    @Test
    void article_id_from_redis_by_user_id_is_exist_false() {
        String redisArticleCountKey = REDIS_ARTICLE_ID_KEY + 12L;
        
        if (!redisTemplate.hasKey(redisArticleCountKey)) {
            redisTemplate.opsForValue().set(redisArticleCountKey, "0");
        }
        
        redisTemplate.opsForValue().increment(redisArticleCountKey);
        
        long id = Long.parseLong(redisTemplate.opsForValue().get(redisArticleCountKey));
        
        assertThat(id).isEqualTo(1L);
    }
    
    @DisplayName("getNewArticleId() 검증 - 기존 회원")
    @Test
    void article_id_from_redis_by_user_id_is_exist_true() {
        String redisArticleCountKey = REDIS_ARTICLE_ID_KEY + 2L;
        
        redisTemplate.opsForValue().set(redisArticleCountKey, "0");
        redisTemplate.opsForValue().increment(redisArticleCountKey);
        redisTemplate.opsForValue().increment(redisArticleCountKey);
        redisTemplate.opsForValue().increment(redisArticleCountKey);
        
        if (!redisTemplate.hasKey(redisArticleCountKey)) {
            redisTemplate.opsForValue().set(redisArticleCountKey, "0");
        }

        redisTemplate.opsForValue().increment(redisArticleCountKey);
        
        long id = Long.parseLong(redisTemplate.opsForValue().get(redisArticleCountKey));
        
        assertThat(id).isEqualTo(4L);
    }
    
    
    @BeforeEach
    void init() {
        initRedisUserInfo(2, "gimwlgus@gmail.com", "얼거스", TYPE_ADMIN);
        initRedisUserInfo(3, "gimwlgus@daum.net", "zhyun", TYPE_MEMBER);
        initRedisUserInfo(5, "gimwlgus@kakao.com", "얼구스", TYPE_MEMBER);
    }
    
    @AfterEach
    void clean() {
        redisTemplate.keys("*").stream()
                .filter(key -> key.startsWith(REDIS_ARTICLE_ID_KEY))
                .map(redisTemplate::delete)
                .close();
    }
    
    private void initRedisUserInfo(long id, String email, String nickname, String grade) {
        jwtUserInfoRepository.save(JwtUserInfoEntity.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .grade("ROLE_" + grade).build());
    }
}