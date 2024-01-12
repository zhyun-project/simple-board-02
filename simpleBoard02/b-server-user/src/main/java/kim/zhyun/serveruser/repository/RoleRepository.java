package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.data.entity.Role;

public interface RoleRepository extends ReadRepository<Role, Long> {
    
    Role findByGrade(String grade);
    boolean existsByGrade(String grade);
    
}
