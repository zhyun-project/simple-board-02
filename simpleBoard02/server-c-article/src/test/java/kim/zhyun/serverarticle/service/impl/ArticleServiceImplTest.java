package kim.zhyun.serverarticle.service.impl;

import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.jwt.data.JwtUserInfo;
import kim.zhyun.jwt.repository.JwtUserInfoRepository;
import kim.zhyun.serverarticle.advice.MemberException;
import kim.zhyun.serverarticle.container.RedisTestContainer;
import kim.zhyun.serverarticle.respository.ArticleRepository;
import kim.zhyun.serverarticle.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static kim.zhyun.serverarticle.data.message.ExceptionMessage.EXCEPTION_NOT_FOUND;
import static kim.zhyun.serverarticle.data.type.RoleType.TYPE_ADMIN;
import static kim.zhyun.serverarticle.data.type.RoleType.TYPE_MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(RedisTestContainer.class)
@SpringBootTest
class ArticleServiceImplTest {
    private final JwtUserInfoRepository jwtUserInfoRepository;
    private final ArticleRepository articleRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${key.redis.articleId}") private String REDIS_ARTICLE_ID_KEY;
    
    public ArticleServiceImplTest(@Autowired JwtUserInfoRepository jwtUserInfoRepository,
                                  @Autowired ArticleRepository articleRepository,
                                  @Autowired RedisTemplate<String, String> redisTemplate) {
        this.jwtUserInfoRepository = jwtUserInfoRepository;
        this.articleRepository = articleRepository;
        this.redisTemplate = redisTemplate;
    }
    
    @DisplayName("redis에서 유저 정보 조회")
    @Test
    void user_info_from_redis() {
        Map<Long, JwtUserDto> jwtUserMap = new HashMap<>();
        jwtUserInfoRepository.findAll()
                .forEach(jwtUserInfo -> jwtUserMap.put(jwtUserInfo.getId(), JwtUserDto.from(jwtUserInfo)));
        
        jwtUserMap.forEach((id, dto) -> log.info("user id : {}, user dto : {}", id, dto));
    }
    
    @DisplayName("getJwtUserDto() 검증 - 존재하는 아이디")
    @Test
    void user_info_from_redis_by_user_id_is_exist_true() {
        JwtUserDto jwtUserDto = JwtUserDto.from(jwtUserInfoRepository.findById(2L).get());
        
        assertThat(jwtUserDto.getId()).isEqualTo(2L);
        log.info("user info : {}", jwtUserDto);
    }
    
    @DisplayName("getJwtUserDto() 검증 - 없는 아이디")
    @Test
    void user_info_from_redis_by_user_id_is_exist_false() {
        Optional<JwtUserInfo> container = jwtUserInfoRepository.findById(100L);
        
        assertThat(container).isEmpty();
    }
    
    @DisplayName("getNewArticleId() 검증 - 신규 가입자")
    @Test
    void article_id_from_redis_by_user_id_is_exist_false() {
        String redisArticleCountKey = REDIS_ARTICLE_ID_KEY + 2L;
        
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
    
    private void initRedisUserInfo(long id, String email, String nickname, String grade) {
        jwtUserInfoRepository.save(JwtUserInfo.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .grade("ROLE_" + grade).build());
    }
}