package kim.zhyun.serveruser.domain.member.repository;

import jakarta.persistence.*;
import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
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
@Entity(name = "users")
public class UserEntity {
    
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(length = 50, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
    
    @Column(length = 30, nullable = false)
    private String nickname;

    @Column(columnDefinition = "bit default 0 comment '탈퇴 여부'")
    private boolean withdrawal;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime modifiedAt;
    
    @ManyToOne
    @JoinColumn(nullable = false)
    private RoleEntity role;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity userEntity)) return false;
        return Objects.equals(getId(), userEntity.getId())
                && Objects.equals(getEmail(), userEntity.getEmail())
                && Objects.equals(getPassword(), userEntity.getPassword())
                && Objects.equals(getNickname(), userEntity.getNickname())
                && Objects.equals(isWithdrawal(), userEntity.isWithdrawal())
                && Objects.equals(getCreatedAt(), userEntity.getCreatedAt())
                && Objects.equals(getModifiedAt(), userEntity.getModifiedAt())
                && Objects.equals(getRole(), userEntity.getRole());
    }
    
}
