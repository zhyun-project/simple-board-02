package kim.zhyun.serverarticle.domain.business;

import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.serverarticle.domain.controller.model.ArticleResponse;
import kim.zhyun.serverarticle.domain.controller.model.ArticleSaveRequest;
import kim.zhyun.serverarticle.domain.controller.model.ArticleUpdateRequest;
import kim.zhyun.serverarticle.domain.controller.model.ArticlesDeleteRequest;
import kim.zhyun.serverarticle.domain.converter.ArticleConverter;
import kim.zhyun.serverarticle.domain.respository.ArticleEntity;
import kim.zhyun.serverarticle.domain.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static kim.zhyun.jwt.exception.message.ExceptionMessage.EXCEPTION_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class ArticleBusiness {
    
    private final ArticleService articleService;
    
    private final ArticleConverter articleConverter;
    
    public List<ArticleResponse> findAll() {
        
        return articleService.findAllOrderByCreatedAtDesc()
                .stream()
                .map(articleConverter::toResponse)
                .toList();
    }
    
    public List<ArticleResponse> findAllByUser(long userId) {
        
        return articleService.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(articleConverter::toResponse)
                .toList();
    }
    
    public ArticleResponse findByArticleId(long userId, long articleId) {
        ArticleEntity articleEntity = articleService.findByUserIdAndArticleId(userId, articleId);
        
        return articleConverter.toResponse(articleEntity);
    }
    
    public ArticleResponse save(ArticleSaveRequest request) {
        ArticleEntity newArticleEntity = articleConverter.toEntity(request);
        ArticleEntity savedArticleEntity = articleService.save(newArticleEntity);
        
        return articleConverter.toResponse(savedArticleEntity);
    }
    
    public ArticleResponse update(ArticleUpdateRequest request) {
        
        ArticleEntity articleEntity = articleService.findByUserIdAndArticleId(request.getUserId(), request.getArticleId());
        
        if (articleEntity.getArticleId() != request.getArticleId() || articleEntity.getUserId() != request.getUserId())
            throw new ApiException(EXCEPTION_NOT_FOUND);
        
        ArticleEntity updatedEntity = articleService.update(articleEntity, request);
        return articleConverter.toResponse(updatedEntity);
    }
    
    public void delete(ArticlesDeleteRequest request) {
        articleService.delete(request);
    }
    
    public String deleteUserAll(Collection<Long> userIds) {
        // return : delete fail message
        return articleService.deleteUserAll(userIds);
    }
    
}
