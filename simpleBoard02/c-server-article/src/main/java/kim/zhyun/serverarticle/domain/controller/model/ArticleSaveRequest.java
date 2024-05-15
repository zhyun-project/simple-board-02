package kim.zhyun.serverarticle.domain.controller.model;

import kim.zhyun.serverarticle.common.annotation.Content;
import kim.zhyun.serverarticle.common.annotation.Title;
import lombok.*;

import java.util.Objects;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter @Builder
public class ArticleSaveRequest {
    
    private long userId;
    
    @Title
    private String title;
    
    @Content
    private String content;
    
    public void setTitle(String title) {
        this.title = title.trim();
    }
    
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleSaveRequest that)) return false;
        return userId == that.userId
                && Objects.equals(title, that.title)
                && Objects.equals(content, that.content);
    }
    @Override public int hashCode() {
        return Objects.hash(userId, title, content);
    }
}
