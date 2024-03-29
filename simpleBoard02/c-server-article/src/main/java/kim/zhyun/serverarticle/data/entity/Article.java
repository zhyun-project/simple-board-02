package kim.zhyun.serverarticle.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

import static jakarta.persistence.GenerationType.IDENTITY;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(indexes = {
        @Index(name = "idx_article_title", columnList = "title"),
        @Index(name = "idx_article_content", columnList = "content"),
        @Index(name = "idx_article_userId", columnList = "userId"),
        @Index(name = "idx_article_userArticleId", columnList = "userId, articleId")
})
public class Article {
    
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "varchar(60)")
    private String title;
    @Column(nullable = false, columnDefinition = "text")
    private String content;
    @Column(nullable = false)
    private long userId;
    @Column(nullable = false)
    private long articleId;
    
    @CreatedDate        private LocalDateTime createdAt;
    @LastModifiedDate   private LocalDateTime modifiedAt;
    
    
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article article)) return false;
        return userId == article.userId
                && articleId == article.articleId
                && Objects.equals(id, article.id)
                && Objects.equals(title, article.title)
                && Objects.equals(content, article.content)
                && Objects.equals(createdAt, article.createdAt)
                && Objects.equals(modifiedAt, article.modifiedAt);
    }
    @Override public int hashCode() {
        return Objects.hash(id, title, content, userId, articleId, createdAt, modifiedAt);
    }
}
