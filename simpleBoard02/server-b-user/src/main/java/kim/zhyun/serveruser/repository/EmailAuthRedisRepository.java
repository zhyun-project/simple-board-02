package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.entity.EmailAuth;
import org.springframework.data.repository.CrudRepository;

public interface EmailAuthRedisRepository extends CrudRepository<EmailAuth, String> {

}
