package kim.zhyun.serverarticle.domain.respository;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends Repository<ArticleEntity, Long> {
    
    List<ArticleEntity> findAllByUserIdOrderByCreatedAtDesc(long userId);
    Optional<ArticleEntity> findByUserIdAndArticleId(long userId, long articleId);
    
    List<ArticleEntity> findAll(Sort createdAt);
    
    ArticleEntity save(ArticleEntity entity);
    
    void deleteAllInBatch(Iterable<ArticleEntity> entities);

}
