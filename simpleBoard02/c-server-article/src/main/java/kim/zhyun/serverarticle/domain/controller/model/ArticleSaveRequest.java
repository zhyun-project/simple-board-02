package kim.zhyun.serverarticle.domain.controller.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Objects;

import static kim.zhyun.serverarticle.common.message.ExceptionMessage.EXCEPTION_CONTENT_IS_NULL;
import static kim.zhyun.serverarticle.common.message.ExceptionMessage.EXCEPTION_TITLE_FORMAT;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter @Builder
public class ArticleSaveRequest {
    
    private long userId;
    
    @Size(min = 1, max = 30, message = EXCEPTION_TITLE_FORMAT)
    private String title;
    
    @NotNull(message = EXCEPTION_CONTENT_IS_NULL)
    private String content;

    
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
