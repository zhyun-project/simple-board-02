package kim.zhyun.serverarticle.domain.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
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
    
    private JwtUserInfoDto user;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    
    
    
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
