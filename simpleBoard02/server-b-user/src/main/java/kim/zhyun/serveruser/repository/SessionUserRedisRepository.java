package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.entity.SessionUser;
import org.springframework.data.repository.CrudRepository;

public interface SessionUserRedisRepository extends CrudRepository<SessionUser, String> {

}
