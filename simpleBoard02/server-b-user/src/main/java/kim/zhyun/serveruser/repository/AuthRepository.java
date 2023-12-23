package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.entity.Auth;
import kim.zhyun.serveruser.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<Auth, Long> {
}
