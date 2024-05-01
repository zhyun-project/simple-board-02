package kim.zhyun.serveruser.domain.signup.repository;

import org.springframework.data.repository.Repository;

import java.util.List;

public interface RoleRepository extends Repository<RoleEntity, Long> {
    
    List<RoleEntity> findAll();
    RoleEntity findByGrade(String grade);
    boolean existsByGrade(String grade);
    
}
