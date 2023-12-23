package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends ReadRepository<Role, Long> {
    
    Optional<Role> findByRole(String role);
    boolean existsByRole(String role);
    
}
