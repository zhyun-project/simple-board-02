package kim.zhyun.serverarticle.domain.service;

import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.jwt.domain.repository.JwtUserInfoRepository;
import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.serverarticle.domain.controller.model.ArticleUpdateRequest;
import kim.zhyun.serverarticle.domain.controller.model.ArticlesDeleteRequest;
import kim.zhyun.serverarticle.domain.respository.ArticleEntity;
import kim.zhyun.serverarticle.domain.respository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static kim.zhyun.jwt.common.constants.type.RoleType.ROLE_WITHDRAWAL;
import static kim.zhyun.jwt.exception.message.ExceptionMessage.EXCEPTION_NOT_FOUND;
import static kim.zhyun.serverarticle.common.message.ExceptionMessage.*;
import static org.springframework.data.domain.Sort.Order.desc;

@RequiredArgsConstructor
@Service
public class ArticleService {
    private final JwtUserInfoRepository jwtUserInfoRepository;
    private final ArticleRepository articleRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${key.redis.articleId}")
    private String REDIS_ARTICLE_ID_KEY;
    
    
    public List<ArticleEntity> findAllOrderByCreatedAtDesc() {
        return articleRepository.findAll(Sort.by(desc("createdAt")));
    }
    
    public List<ArticleEntity> findAllByUserIdOrderByCreatedAtDesc(long userId) {
        return articleRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public ArticleEntity findByUserIdAndArticleId(long userId, long articleId) {
        Optional<ArticleEntity> articleContainer = articleRepository.findByUserIdAndArticleId(userId, articleId);
        
        if (articleContainer.isEmpty())
            throw new ApiException(EXCEPTION_ARTICLE_NOT_FOUND);
        
        return articleContainer.get();
    }
    
    public ArticleEntity save(ArticleEntity newArticleEntity) {
        long newArticleId = getNewArticleId(newArticleEntity.getUserId());
        
        newArticleEntity.setArticleId(newArticleId);
        
        return articleRepository.save(newArticleEntity);
    }
    
    @Transactional
    public ArticleEntity update(ArticleEntity articleEntity, ArticleUpdateRequest request) {
        articleEntity.setTitle(request.getTitle());
        articleEntity.setContent(request.getContent());
        return articleEntity;
    }
    
    public void delete(ArticlesDeleteRequest request) {
        Set<ArticleEntity> deleteSet = articleRepository.findAllByUserIdOrderByCreatedAtDesc(request.getUserId())
                .stream()
                .filter(article -> request.getArticleIds().contains(article.getArticleId()))
                .collect(Collectors.toSet());
        
        articleRepository.deleteAllInBatch(deleteSet);
    }
    
    public String deleteUserAll(Collection<Long> userIds) {
        String failMessage = userIds.stream()
                .filter(userId -> {
                    Optional<JwtUserInfoEntity> container = jwtUserInfoRepository.findById(userId);
                    
                    if (container.isPresent() && container.get().getGrade().equals(ROLE_WITHDRAWAL)) {
                        List<ArticleEntity> list = articleRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
                        articleRepository.deleteAllInBatch(list);
                        redisTemplate.delete(REDIS_ARTICLE_ID_KEY + userId);
                        return false;
                    }
                    
                    return true;
                })
                .map(userId -> {
                    Optional<JwtUserInfoEntity> container = jwtUserInfoRepository.findById(userId);
                    
                    if (container.isEmpty())
                        return EXCEPTION_DELETED_WITHDRAWAL.formatted(userId);
                    
                    if (!container.get().getGrade().equals(ROLE_WITHDRAWAL))
                        return EXCEPTION_NOT_WITHDRAWAL.formatted(userId, container.get().getEmail());
                    
                    return "";
                })
                .collect(Collectors.joining());
        
        return failMessage;
    }
    
    /**
     * `redis`에서 `user` 정보 가져와서 반환
     */
    public JwtUserInfoEntity findJwtUserInfoEntityByIdWithThrow(long userId) {
        Optional<JwtUserInfoEntity> jwtUserContainer = jwtUserInfoRepository.findById(userId);
        
        if (jwtUserContainer.isEmpty())
            throw new ApiException(EXCEPTION_NOT_FOUND);
        
        return jwtUserContainer.get();
    }
    
    
    /**
     * `user`의 `article id` 계산 후 반환
     */
    private long getNewArticleId(long userId) {
        String redisArticleCountKey = REDIS_ARTICLE_ID_KEY + userId;
        
        if (!redisTemplate.hasKey(redisArticleCountKey)) {
            redisTemplate.opsForValue().set(redisArticleCountKey, "0");
        }
        
        redisTemplate.opsForValue().increment(redisArticleCountKey);
        
        return Long.parseLong(redisTemplate.opsForValue().get(redisArticleCountKey));
    }
    
}
