package kim.zhyun.serveruser.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter @Builder
@Entity
public class Role {
    
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, updatable = false)
    private String role;
    private String description;
    
}
