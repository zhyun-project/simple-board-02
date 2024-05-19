package kim.zhyun.serveruser.domain.signup.service;

import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.jwt.domain.repository.JwtUserInfoRepository;
import kim.zhyun.serveruser.common.value.SignUpValue;
import kim.zhyun.serveruser.domain.member.repository.UserEntity;
import kim.zhyun.serveruser.domain.member.repository.UserRepository;
import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
import kim.zhyun.serveruser.domain.signup.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import static kim.zhyun.jwt.common.constants.type.RoleType.TYPE_ADMIN;
import static kim.zhyun.jwt.common.constants.type.RoleType.TYPE_MEMBER;


@RequiredArgsConstructor
@Service
public class SignUpService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final JwtUserInfoRepository jwtUserInfoRepository;
    
    private final SignUpValue value;
    
    
    public boolean isAvailableEmail(String email) {
        return !userRepository.existsByEmailIgnoreCase(email);
    }
    
    public boolean isUsedNickname(String nickname) {
        return userRepository.existsByNicknameIgnoreCase(nickname);
    }
    
    public RoleEntity getGrade(String email) {
        // admin 유저 구분
        String admins = Strings.join(value.adminEmails, ',');
        String grade  = admins == null || !admins.contains(email)
                ? TYPE_MEMBER
                : TYPE_ADMIN;
        
        return roleRepository.findByGrade(grade);
    }
    
    public UserEntity saveUser(UserEntity entity) {
        return userRepository.save(entity);
    }
    
    /**
     * redis user info 저장소 업데이트
     */
    public void jwtUserInfoUpdate(UserEntity userEntity) {
        jwtUserInfoRepository.save(JwtUserInfoEntity.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .nickname(userEntity.getNickname())
                .grade("ROLE_" + userEntity.getRole().getGrade())
                .build());
    }
    
}
