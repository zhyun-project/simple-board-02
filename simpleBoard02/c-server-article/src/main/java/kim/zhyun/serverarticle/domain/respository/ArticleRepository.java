package kim.zhyun.serverarticle.domain.respository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<ArticleEntity, Long> {
    
    List<ArticleEntity> findAllByUserIdOrderByCreatedAtDesc(long userId);
    Optional<ArticleEntity> findByUserIdAndArticleId(long userId, long articleId);
    
}
