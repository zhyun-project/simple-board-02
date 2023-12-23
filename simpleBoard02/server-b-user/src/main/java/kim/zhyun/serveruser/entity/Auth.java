package kim.zhyun.serveruser.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter @Builder
@Entity
public class Auth {
    
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @ColumnDefault("false")
    @Builder.Default
    private boolean isVerification = false;
    
    private String code;
    private LocalDateTime expiredAt;
    
    @OneToOne
    private User user;
}
