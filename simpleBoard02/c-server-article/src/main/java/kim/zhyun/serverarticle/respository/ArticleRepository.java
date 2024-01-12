package kim.zhyun.serverarticle.respository;

import kim.zhyun.serverarticle.data.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    List<Article> findAllByUserIdOrderByCreatedAtDesc(long userId);
    Optional<Article> findByUserIdAndArticleId(long userId, long articleId);
    
}
