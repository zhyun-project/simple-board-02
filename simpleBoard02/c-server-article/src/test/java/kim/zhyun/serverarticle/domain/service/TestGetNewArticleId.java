package kim.zhyun.serverarticle.domain.service;


import kim.zhyun.serverarticle.common.value.ArticleValue;
import kim.zhyun.serverarticle.container.RedisTestContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Disabled("필요시 사용")
@DisplayName("articleId 생성 확인")
@SpringBootTest
@ExtendWith(RedisTestContainer.class)
public class TestGetNewArticleId {
    
    @Autowired RedisTemplate<String, String> redisTemplate;
    @Autowired ArticleValue articleValue;
    
    static long userId = 1L;
    static long doNewArticleId = 1L;
    
    
    @RepeatedTest(5)
    void get_new_article_id() {
        // given
        String redisArticleCountKey = articleValue.REDIS_ARTICLE_ID_KEY + userId;
        
        
        // when
        Long resultNewArticleId = redisTemplate.opsForValue().increment(redisArticleCountKey);
        
        
        // then
        assertEquals(doNewArticleId++, resultNewArticleId);
    }
    
}
