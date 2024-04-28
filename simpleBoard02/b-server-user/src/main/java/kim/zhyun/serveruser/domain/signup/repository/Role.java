package kim.zhyun.serveruser.domain.signup.repository;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;

import java.util.Objects;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Entity
public class Role {
    
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, updatable = false)
    private String grade;
    private String description;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return Objects.equals(getId(), role.getId())
                && Objects.equals(getGrade(), role.getGrade())
                && Objects.equals(getDescription(), role.getDescription());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), getGrade(), getDescription());
    }
    
}
