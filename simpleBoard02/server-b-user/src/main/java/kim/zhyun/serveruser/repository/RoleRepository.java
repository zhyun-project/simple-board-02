package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.data.entity.Role;

import java.util.Optional;

public interface RoleRepository extends ReadRepository<Role, Long> {
    
    Optional<Role> findByRoleIgnoreCase(String role);
    boolean existsByRole(String role);
    
}
