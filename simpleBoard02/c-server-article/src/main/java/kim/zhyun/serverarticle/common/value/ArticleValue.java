package kim.zhyun.serverarticle.common.value;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ArticleValue {
    
    public final String REDIS_ARTICLE_ID_KEY;

    
    public ArticleValue(
            @Value("${key.redis.articleId}") String redisArticleKey
    ) {
        this.REDIS_ARTICLE_ID_KEY = redisArticleKey;
    }
}
