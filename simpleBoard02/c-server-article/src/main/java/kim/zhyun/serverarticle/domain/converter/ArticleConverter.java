package kim.zhyun.serverarticle.domain.converter;

import kim.zhyun.jwt.domain.converter.JwtUserInfoConverter;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.serverarticle.domain.controller.model.ArticleResponse;
import kim.zhyun.serverarticle.domain.controller.model.ArticleSaveRequest;
import kim.zhyun.serverarticle.domain.respository.ArticleEntity;
import kim.zhyun.serverarticle.domain.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ArticleConverter {
    
    private final ArticleService articleService;
    private final JwtUserInfoConverter jwtUserInfoConverter;
    
    public ArticleResponse toResponse(ArticleEntity source) {
        JwtUserInfoEntity jwtUserInfoEntity = articleService.findJwtUserInfoEntityByIdWithThrow(source.getUserId());
        JwtUserInfoDto jwtUserInfoDto = jwtUserInfoConverter.toDto(jwtUserInfoEntity);
        
        return ArticleResponse.builder()
                .id(source.getId())
                .articleId(source.getArticleId())
                .user(jwtUserInfoDto)
                
                .title(source.getTitle())
                .content(source.getContent())
                
                .createdAt(source.getCreatedAt())
                .modifiedAt(source.getModifiedAt()).build();
    }
    
    public ArticleEntity toEntity(ArticleSaveRequest request) {
        return ArticleEntity.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .content(request.getContent()).build();
    }
}
