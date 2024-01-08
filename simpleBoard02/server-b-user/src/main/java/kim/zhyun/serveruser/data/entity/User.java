package kim.zhyun.serveruser.data.entity;

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
@Builder
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "users")
public class User {
    
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(length = 50)
    private String email;
    private String password;
    @Column(length = 30)
    private String nickname;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime modifiedAt;
    
    @ManyToOne
    private Role role;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(getId(), user.getId())
                && Objects.equals(getEmail(), user.getEmail())
                && Objects.equals(getPassword(), user.getPassword())
                && Objects.equals(getNickname(), user.getNickname())
                && Objects.equals(getCreatedAt(), user.getCreatedAt())
                && Objects.equals(getModifiedAt(), user.getModifiedAt())
                && Objects.equals(getRole(), user.getRole());
    }
    
}
