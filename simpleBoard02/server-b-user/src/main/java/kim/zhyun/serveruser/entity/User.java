package kim.zhyun.serveruser.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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
    
    @OneToOne
    private Role role;
    
}
