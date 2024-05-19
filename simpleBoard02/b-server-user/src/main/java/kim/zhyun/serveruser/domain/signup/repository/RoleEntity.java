package kim.zhyun.serveruser.domain.signup.repository;


import jakarta.persistence.*;
import lombok.Getter;

import java.util.Objects;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Entity(name = "role")
public class RoleEntity {
    
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, updatable = false)
    private String grade;
    private String description;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleEntity roleEntity)) return false;
        return Objects.equals(getId(), roleEntity.getId())
                && Objects.equals(getGrade(), roleEntity.getGrade())
                && Objects.equals(getDescription(), roleEntity.getDescription());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), getGrade(), getDescription());
    }
    
}
