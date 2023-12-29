package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.data.entity.Role;

public interface RoleRepository extends ReadRepository<Role, Long> {
    
    Role findByRole(String role);
    boolean existsByRole(String role);
    
}
