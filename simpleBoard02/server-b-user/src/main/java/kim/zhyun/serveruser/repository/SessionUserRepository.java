package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.data.entity.SessionUser;
import org.springframework.data.repository.CrudRepository;

public interface SessionUserRepository extends CrudRepository<SessionUser, String> {

}
