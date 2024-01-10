package kim.zhyun.serverarticle.data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Objects;

import static kim.zhyun.serverarticle.data.message.ExceptionMessage.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter @Builder
public class ArticleUpdateRequest {
    
    private long id;
    private long articleId;
    private long userId;
    
    @NotNull(message = EXCEPTION_TITLE_IS_NULL)
    @Size(min = 1, max = 30, message = EXCEPTION_TITLE_FORMAT)
    private String title;
    
    @NotNull(message = EXCEPTION_CONTENT_IS_NULL)
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
