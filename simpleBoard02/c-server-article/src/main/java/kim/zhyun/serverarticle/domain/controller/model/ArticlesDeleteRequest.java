package kim.zhyun.serverarticle.domain.controller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Builder
public class ArticlesDeleteRequest {
    
    private long userId;
    private Collection<Long> articleIds;

    
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticlesDeleteRequest that)) return false;
        return getUserId() == that.getUserId() && Objects.equals(getArticleIds(), that.getArticleIds());
    }
    @Override public int hashCode() {
        return Objects.hash(getUserId(), getArticleIds());
    }
}
