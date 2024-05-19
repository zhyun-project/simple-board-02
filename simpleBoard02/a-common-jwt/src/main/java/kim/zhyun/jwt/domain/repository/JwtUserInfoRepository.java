package kim.zhyun.jwt.domain.repository;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface JwtUserInfoRepository extends Repository<JwtUserInfoEntity, Long> {
    
    Optional<JwtUserInfoEntity> findById(Long id);
    List<JwtUserInfoEntity> findAll();
    JwtUserInfoEntity save(JwtUserInfoEntity entity);
    
}
