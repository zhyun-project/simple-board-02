package kim.zhyun.serverarticle.domain.controller.model;

import kim.zhyun.serverarticle.common.annotation.Content;
import kim.zhyun.serverarticle.common.annotation.Title;
import lombok.*;

import java.util.Objects;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter @Builder
public class ArticleUpdateRequest {
    
    private long id;
    private long articleId;
    private long userId;
    
    @Title
    private String title;
    
    @Content
    private String content;

    
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleUpdateRequest that)) return false;
        return getArticleId() == that.getArticleId()
                && getUserId() == that.getUserId()
                && Objects.equals(getTitle(), that.getTitle())
                && Objects.equals(getContent(), that.getContent());
    }
    @Override public int hashCode() {
        return Objects.hash(getArticleId(), getUserId(), getTitle(), getContent());
    }
}
