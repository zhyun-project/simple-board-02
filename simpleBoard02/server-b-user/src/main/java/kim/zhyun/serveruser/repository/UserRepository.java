package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByNicknameIgnoreCase(String nickname);
    
}
