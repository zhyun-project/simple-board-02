package kim.zhyun.serverarticle.service.impl;

import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.jwt.data.JwtUserInfo;
import kim.zhyun.jwt.repository.JwtUserInfoRepository;
import kim.zhyun.serverarticle.advice.ArticleException;
import kim.zhyun.serverarticle.advice.MemberException;
import kim.zhyun.serverarticle.data.ArticleSaveRequest;
import kim.zhyun.serverarticle.data.ArticleUpdateRequest;
import kim.zhyun.serverarticle.data.ArticlesDeleteRequest;
import kim.zhyun.serverarticle.data.entity.Article;
import kim.zhyun.serverarticle.data.response.ArticleResponse;
import kim.zhyun.serverarticle.respository.ArticleRepository;
import kim.zhyun.serverarticle.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static kim.zhyun.serverarticle.data.message.ExceptionMessage.EXCEPTION_ARTICLE_NOT_FOUND;
import static kim.zhyun.serverarticle.data.message.ExceptionMessage.EXCEPTION_NOT_FOUND;
import static org.springframework.data.domain.Sort.Order.desc;

@RequiredArgsConstructor
@Service
public class ArticleServiceImpl implements ArticleService {
    private final JwtUserInfoRepository jwtUserInfoRepository;
    private final ArticleRepository articleRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${key.redis.articleId}")
    private String REDIS_ARTICLE_ID_KEY;
    
    
    @Override
    public List<ArticleResponse> findAll() {
        Map<Long, JwtUserDto> jwtUserMap = new HashMap<>();
        
        jwtUserInfoRepository.findAll()
                .forEach(jwtUserInfo -> jwtUserMap.put(jwtUserInfo.getId(), JwtUserDto.from(jwtUserInfo)));
        
        return articleRepository.findAll(Sort.by(desc("createdAt"))).stream()
                .map(article -> ArticleResponse.from(article, jwtUserMap.get(article.getUserId())))
                .toList();
    }
    
    @Override
    public List<ArticleResponse> findAllByUser(long userId) {
        JwtUserDto jwtUserDto = getJwtUserDto(userId);
        
        return articleRepository.findAll(Sort.by(desc("createdAt"))).stream()
                .map(article -> ArticleResponse.from(article, jwtUserDto))
                .toList();
    }
    
    @Override
    public ArticleResponse findByArticleId(long userId, long articleId) {
        JwtUserDto jwtUserDto = getJwtUserDto(userId);
        
        Optional<Article> articleContainer = articleRepository.findByUserIdAndArticleId(userId, articleId);
        
        if (articleContainer.isEmpty())
            throw new ArticleException(EXCEPTION_ARTICLE_NOT_FOUND);
        
        return ArticleResponse.from(articleContainer.get(), jwtUserDto);
    }
    
    @Override
    public ArticleResponse save(ArticleSaveRequest request) {
        JwtUserDto jwtUserDto = getJwtUserDto(request.getUserId());
        long newArticleId = getNewArticleId(request.getUserId());
        
        Article saved = articleRepository.save(Article.builder()
                .userId(request.getUserId())
                .articleId(newArticleId)
                .title(request.getTitle())
                .content(request.getContent()).build());
        
        return ArticleResponse.from(saved, jwtUserDto);
    }

    @Transactional
    @Override
    public void update(ArticleUpdateRequest request) {
        JwtUserDto jwtUserDto = getJwtUserDto(request.getUserId());
        Optional<Article> articleContainer = articleRepository.findById(request.getId());
        
        if (articleContainer.isEmpty())
            throw new ArticleException(EXCEPTION_ARTICLE_NOT_FOUND);
        
        Article article = articleContainer.get();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
    }
    
    @Override
    public void delete(ArticlesDeleteRequest request) {
        long userId = request.getUserId();
        Collection<Long> articleIds = request.getArticleIds();
        
        Set<Article> deleteSet = articleRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(article -> articleIds.contains(article.getArticleId()))
                .collect(Collectors.toSet());
        
        articleRepository.deleteAllInBatch(deleteSet);
    }
    
    @Override
    public void deleteUserAll(long userId) {
        List<Article> list = articleRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        articleRepository.deleteAllInBatch(list);
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
    
    /**
     * `redis`에서 `user` 정보 가져와서 반환
     */
    @Override
    public JwtUserDto getJwtUserDto(long userId) {
        Optional<JwtUserInfo> jwtUserContainer = jwtUserInfoRepository.findById(userId);
        
        if (jwtUserContainer.isEmpty())
            throw new MemberException(EXCEPTION_NOT_FOUND);
        
        return JwtUserDto.from(jwtUserContainer.get());
    }
}
