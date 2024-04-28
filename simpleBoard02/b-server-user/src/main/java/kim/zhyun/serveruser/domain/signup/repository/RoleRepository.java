package kim.zhyun.serveruser.domain.signup.repository;

import org.springframework.data.repository.Repository;

import java.util.List;

public interface RoleRepository extends Repository<Role, Long> {
    
    List<Role> findAll();
    Role findByGrade(String grade);
    boolean existsByGrade(String grade);
    
}
