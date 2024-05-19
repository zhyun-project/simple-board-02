package kim.zhyun.serveruser.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    // 회원 가입
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByNicknameIgnoreCase(String nickname);
    
    // 로그인
    Optional<UserEntity> findByEmail(String email);
    
    // 탈퇴 회원 조회
    List<UserEntity> findAllByWithdrawalIsTrueOrderByModifiedAtAsc();
}
