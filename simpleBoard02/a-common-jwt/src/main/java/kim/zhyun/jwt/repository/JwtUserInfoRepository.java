package kim.zhyun.jwt.repository;

import kim.zhyun.jwt.data.JwtUserInfo;
import org.springframework.data.repository.CrudRepository;

public interface JwtUserInfoRepository extends CrudRepository<JwtUserInfo, Long> {
}
