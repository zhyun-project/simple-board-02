package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // 회원 가입
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByNicknameIgnoreCase(String nickname);
    
    // 로그인
    Optional<User> findByEmail(String email);
    
}
