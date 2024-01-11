package kim.zhyun.serverarticle.service;

import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.serverarticle.data.ArticleSaveRequest;
import kim.zhyun.serverarticle.data.ArticlesDeleteRequest;
import kim.zhyun.serverarticle.data.ArticleUpdateRequest;
import kim.zhyun.serverarticle.data.response.ArticleResponse;

import java.util.List;

public interface ArticleService {
    
    List<ArticleResponse> findAll();
    List<ArticleResponse> findAllByUser(long userId);
    ArticleResponse findByArticleId(long userId, long articleId);
    ArticleResponse save(ArticleSaveRequest request);
    void update(ArticleUpdateRequest request);
    void delete(ArticlesDeleteRequest request);
    void deleteUserAll(long userId);
    
    JwtUserDto getJwtUserDto(long userId);
    
}
