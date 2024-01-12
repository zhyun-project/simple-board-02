package kim.zhyun.serveruser.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface ReadRepository <T, ID> extends Repository<T, ID> {
    
    List<T> findAll();
    Optional<T> findById(ID id);
    
}
