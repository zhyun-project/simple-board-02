package kim.zhyun.serveruser.domain.member.service;

import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.jwt.domain.repository.JwtUserInfoRepository;
import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.serveruser.domain.member.controller.model.UserGradeUpdateRequest;
import kim.zhyun.serveruser.domain.member.controller.model.UserUpdateRequest;
import kim.zhyun.serveruser.domain.member.repository.UserEntity;
import kim.zhyun.serveruser.domain.member.repository.UserRepository;
import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
import kim.zhyun.serveruser.domain.signup.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static kim.zhyun.jwt.common.constants.type.RoleType.TYPE_WITHDRAWAL;
import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_SIGNIN_FAIL;
import static org.springframework.data.domain.Sort.Order.asc;


@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    private final UserRepository userRepository;
    private final JwtUserInfoRepository jwtUserInfoRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;


    /**
     * 회원 정보 id 오름차순 조회
     */
    public List<UserEntity> findAll() {
        return userRepository.findAll(Sort.by(asc("id")));
    }
    
    /**
     * id로 user entity 반환
     * 없는 경우 예외 출력
     */
    public UserEntity findByIdWithThrow(long userId) {
        Optional<UserEntity> userContainer = userRepository.findById(userId);
        
        if (userContainer.isEmpty())
            throw new ApiException(EXCEPTION_SIGNIN_FAIL);
        
        return userContainer.get();
    }
    
    /**
     * 이메일로 user entity 반환
     * 없으면 예외 출력
     */
    public UserEntity findByEmailWithThrow(String email) {
        Optional<UserEntity> userContainer = userRepository.findByEmail(email);
        
        if (userContainer.isEmpty())
            throw new ApiException(EXCEPTION_SIGNIN_FAIL);
        
        return userContainer.get();
    }
    
    /**
     * 계정 정보 업데이트
     */
    public UserEntity updateUserInfo(boolean nicknameUpdate, boolean passwordUpdate, UserUpdateRequest request) {
        UserEntity userEntity = findByIdWithThrow(request.getId());
        
        // 닉네임 업데이트
        if (nicknameUpdate) {
            userEntity.setNickname(request.getNickname());
        }
        
        // 비밀번호 업데이트
        if (passwordUpdate) {
            userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        UserEntity saved = userRepository.save(userEntity);
        jwtUserInfoUpdate(saved);
        
        return saved;
    }
    
    /**
     * 계정 권한 수정
     */
    public UserEntity updateUserGrade(UserGradeUpdateRequest request) {
        UserEntity userEntity = findByIdWithThrow(request.getId());
        
        String roleType = request.getRole().toUpperCase();
        
        userSetRole(userEntity, roleType);
        
        UserEntity saved = userRepository.save(userEntity);
        jwtUserInfoUpdate(saved);
        return saved;
    }

    /**
     * 회원 탈퇴
     */
    public UserEntity withdrawal(long userId) {
        UserEntity userEntity = findByIdWithThrow(userId);

        userSetRole(userEntity, TYPE_WITHDRAWAL);
        
        UserEntity updated = userRepository.save(userEntity);
        jwtUserInfoUpdate(updated);
        
        return updated;
    }

    
    
    /**
     * 회원 권한 설정
     */
    private void userSetRole(UserEntity userEntity, String roleType) {
        RoleEntity roleEntity = roleRepository.findByGrade(roleType);
        userEntity.setRole(roleEntity);
        userEntity.setWithdrawal(TYPE_WITHDRAWAL.equals(roleType));
    }
    
    /**
     * redis user info 저장소 업데이트
     */
    private void jwtUserInfoUpdate(UserEntity userEntity) {
        jwtUserInfoRepository.save(JwtUserInfoEntity.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .nickname(userEntity.getNickname())
                .grade("ROLE_" + userEntity.getRole().getGrade())
                .build());
    }
}
