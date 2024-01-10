package kim.zhyun.serverarticle.data.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.serverarticle.data.ArticlesDeleteRequest;
import kim.zhyun.serverarticle.data.entity.Article;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter @Builder
@JsonInclude(NON_NULL)
public class ArticleResponse {
    
    private long id;
    private long articleId;
    private String title;
    private String content;
    
    private JwtUserDto user;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    
    public static ArticleResponse from(Article source, JwtUserDto user) {
        return ArticleResponse.builder()
                .id(source.getId())
                .articleId(source.getArticleId())
                .user(user)
                
                .title(source.getTitle())
                .content(source.getContent())
                
                .createdAt(source.getCreatedAt())
                .modifiedAt(source.getModifiedAt()).build();
    }
    
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleResponse that)) return false;
        return id == that.id
                && articleId == that.articleId
                && Objects.equals(title, that.title)
                && Objects.equals(content, that.content)
                && Objects.equals(user, that.user);
    }
    @Override public int hashCode() {
        return Objects.hash(id, articleId, title, content, user);
    }
}
