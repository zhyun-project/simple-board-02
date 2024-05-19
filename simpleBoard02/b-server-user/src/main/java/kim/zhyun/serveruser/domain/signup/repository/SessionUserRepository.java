package kim.zhyun.serveruser.domain.signup.repository;

import kim.zhyun.serveruser.domain.signup.repository.SessionUser;
import org.springframework.data.repository.CrudRepository;

public interface SessionUserRepository extends CrudRepository<SessionUser, String> {

}
