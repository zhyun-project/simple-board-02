package kim.zhyun.serveruser.domain.member.service;

import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.jwt.domain.repository.JwtUserInfoRepository;
import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.serveruser.domain.member.controller.model.UserGradeUpdateRequest;
import kim.zhyun.serveruser.domain.member.controller.model.UserUpdateRequest;
import kim.zhyun.serveruser.domain.member.repository.UserEntity;
import kim.zhyun.serveruser.domain.member.repository.UserRepository;
import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
import kim.zhyun.serveruser.domain.signup.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_SIGNIN_FAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.data.domain.Sort.Order.asc;

@DisplayName("회원 service test")
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks MemberService memberService;

    @Mock UserRepository userRepository;
    @Mock JwtUserInfoRepository jwtUserInfoRepository;
    @Mock RoleRepository roleRepository;
    
    @Mock PasswordEncoder passwordEncoder;

    
    
    @DisplayName("회원 정보 id 전체 조회(id 오름차순 정렬)")
    @Test
    void find_all() {
        given(userRepository.findAll(Sort.by(asc("id")))).willReturn(
                List.of(
                        getUserEntity(1L),
                        getUserEntity(2L),
                        getUserEntity(3L)
                )
        );
        
        // when
        List<UserEntity> resultList = memberService.findAll();
        
        // then
        assertAll(
                () -> assertTrue(resultList.get(0).getId() < resultList.get(1).getId()),
                () -> assertTrue(resultList.get(1).getId() < resultList.get(2).getId())
        );
        
        then(userRepository).should(times(1)).findAll(any(Sort.class));
    }
    
    @DisplayName("userId로 user entity 조회 - 성공")
    @Test
    void find_by_id_with_throw_success() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(getUserEntity(anyLong())));
        
        // when
        UserEntity result = memberService.findByIdWithThrow(1L);
        
        // then
        assertNotNull(result);
        then(userRepository).should(times(1)).findById(anyLong());
    }
    
    @DisplayName("userId로 user entity 조회 - 실패")
    @Test
    void find_by_id_with_throw_fail() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        
        // when-then
        assertThrows(
                ApiException.class,
                () -> memberService.findByIdWithThrow(1L),
                EXCEPTION_SIGNIN_FAIL
        );
        then(userRepository).should(times(1)).findById(anyLong());
    }
    
    @DisplayName("user email로 user entity 조회 - 성공")
    @Test
    void find_by_email_with_throw_success() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(getUserEntity(anyLong())));
        
        // when
        UserEntity result = memberService.findByEmailWithThrow("gimwlgus@email.mail");
        
        // then
        assertNotNull(result);
        then(userRepository).should(times(1)).findByEmail(anyString());
    }
    
    @DisplayName("user email로 user entity 조회 - 실패")
    @Test
    void find_by_email_with_throw_fail() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        
        // when-then
        assertThrows(
                ApiException.class,
                () -> memberService.findByEmailWithThrow("gimwlgus@email.mail"),
                EXCEPTION_SIGNIN_FAIL
        );
        then(userRepository).should(times(1)).findByEmail(anyString());
    }
    
    
    @DisplayName("계정 정보 업데이트 - nickname, password 수정")
    @Test
    void update_user_info() {
        // given
        boolean nicknameUpdate = true;
        boolean passwordUpdate = true;

        long userId = 1L;
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .id(userId)
                .email("gimwlgus@email.mail")
                .nickname("update-nickname")
                .password("update-password")
                .build();
        
        // -- find user entity by user id
        UserEntity userEntity = getUserEntity(userId);
        given(userRepository.findById(userEntity.getId())).willReturn(Optional.of(userEntity));
        
        // -- update user entity
        String encodedPassword = "encoded-update-password";
        given(passwordEncoder.encode(userUpdateRequest.getPassword())).willReturn(encodedPassword);
        
        userEntity.setNickname(userUpdateRequest.getNickname());
        userEntity.setPassword(encodedPassword);
        
        UserEntity updateUserEntity;
        given(userRepository.save(userEntity)).willReturn(updateUserEntity = userEntity);
        
        // -- update redis user info entity
        JwtUserInfoEntity jwtUserInfoEntity = JwtUserInfoEntity.builder()
                .id(updateUserEntity.getId())
                .email(updateUserEntity.getEmail())
                .nickname(updateUserEntity.getNickname())
                .grade("ROLE_" + updateUserEntity.getRole().getGrade())
                .build();
        given(jwtUserInfoRepository.save(jwtUserInfoEntity)).willReturn(jwtUserInfoEntity);
        
        // when
        UserEntity savedUserEntity = memberService.updateUserInfo(nicknameUpdate, passwordUpdate, userUpdateRequest);
        
        // then
        assertThat(updateUserEntity).usingRecursiveComparison().isEqualTo(savedUserEntity);
        
        then(userRepository).should(times(1)).findById(userEntity.getId());
        then(passwordEncoder).should(times(1)).encode(userUpdateRequest.getPassword());
        then(userRepository).should(times(1)).save(userEntity);
        then(jwtUserInfoRepository).should(times(1)).save(jwtUserInfoEntity);
    }
    
    @DisplayName("계정 정보 업데이트 - password 수정")
    @Test
    void update_user_info_only_password() {
        // given
        boolean nicknameUpdate = false;
        boolean passwordUpdate = true;
        
        long userId = 1L;
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .id(userId)
                .email("gimwlgus@email.mail")
                .nickname("nickname")
                .password("update-password")
                .build();
        
        // -- find user entity by user id
        UserEntity userEntity = getUserEntity(userId);
        given(userRepository.findById(userEntity.getId())).willReturn(Optional.of(userEntity));
        
        // -- update user entity
        String encodedPassword = "base64-encoded-password";
        given(passwordEncoder.encode(userUpdateRequest.getPassword())).willReturn(encodedPassword);
        
        userEntity.setPassword(encodedPassword);
        
        UserEntity updateUserEntity;
        given(userRepository.save(userEntity)).willReturn(updateUserEntity = userEntity);
        
        // -- update redis user info entity
        JwtUserInfoEntity jwtUserInfoEntity = JwtUserInfoEntity.builder()
                .id(updateUserEntity.getId())
                .email(updateUserEntity.getEmail())
                .nickname(updateUserEntity.getNickname())
                .grade("ROLE_" + updateUserEntity.getRole().getGrade())
                .build();
        given(jwtUserInfoRepository.save(jwtUserInfoEntity)).willReturn(jwtUserInfoEntity);
        
        // when
        UserEntity savedUserEntity = memberService.updateUserInfo(nicknameUpdate, passwordUpdate, userUpdateRequest);
        
        // then
        assertThat(updateUserEntity).usingRecursiveComparison().isEqualTo(savedUserEntity);
        
        then(userRepository).should(times(1)).findById(userEntity.getId());
        then(passwordEncoder).should(times(1)).encode(userUpdateRequest.getPassword());
        then(userRepository).should(times(1)).save(userEntity);
        then(jwtUserInfoRepository).should(times(1)).save(jwtUserInfoEntity);
    }
    
    
    @DisplayName("계정 정보 업데이트 - nickname 수정")
    @Test
    void update_user_info_only_nickname() {
        // given
        boolean nicknameUpdate = true;
        boolean passwordUpdate = false;

        long userId = 1L;
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .id(userId)
                .email("gimwlgus@email.mail")
                .nickname("update-nickname")
                .password("password")
                .build();
        
        // -- find user entity by user id
        UserEntity userEntity = getUserEntity(userId);
        given(userRepository.findById(userEntity.getId())).willReturn(Optional.of(userEntity));
        
        // -- update user entity
        userEntity.setNickname(userUpdateRequest.getNickname());
        
        UserEntity updateUserEntity;
        given(userRepository.save(userEntity)).willReturn(updateUserEntity = userEntity);
        
        // -- update redis user info entity
        JwtUserInfoEntity jwtUserInfoEntity = JwtUserInfoEntity.builder()
                .id(updateUserEntity.getId())
                .email(updateUserEntity.getEmail())
                .nickname(updateUserEntity.getNickname())
                .grade("ROLE_" + updateUserEntity.getRole().getGrade())
                .build();
        given(jwtUserInfoRepository.save(jwtUserInfoEntity)).willReturn(jwtUserInfoEntity);
        
        // when
        UserEntity savedUserEntity = memberService.updateUserInfo(nicknameUpdate, passwordUpdate, userUpdateRequest);
        
        // then
        assertThat(updateUserEntity).usingRecursiveComparison().isEqualTo(savedUserEntity);
        
        then(userRepository).should(times(1)).findById(userEntity.getId());
        then(passwordEncoder).should(times(0)).encode(userUpdateRequest.getPassword());
        then(userRepository).should(times(1)).save(userEntity);
        then(jwtUserInfoRepository).should(times(1)).save(jwtUserInfoEntity);
    }
    
    
    @DisplayName("계정 권한 수정")
    @ParameterizedTest(name = "{index}. {1}")
    @MethodSource
    void update_user_grade(
            UserGradeUpdateRequest userGradeUpdateRequest,
            /* name 출력 용 */ String memberType
    ) {
        // given
        // -- find user entity by user id
        UserEntity userEntity = getUserEntity(userGradeUpdateRequest.getId());
        given(userRepository.findById(userEntity.getId())).willReturn(Optional.of(userEntity));
        
        // -- find role entity by role type(String)
        RoleEntity roleEntity = new RoleEntity();
        given(roleRepository.findByGrade(userGradeUpdateRequest.getRole())).willReturn(roleEntity);
        
        // -- update user entity
        userEntity.setRole(roleEntity);
        userEntity.setWithdrawal(RoleType.TYPE_WITHDRAWAL.equals(userGradeUpdateRequest.getRole()));
        
        UserEntity updateUserEntity;
        given(userRepository.save(userEntity)).willReturn(updateUserEntity = userEntity);
        
        // -- update redis user info entity
        JwtUserInfoEntity jwtUserInfoEntity = JwtUserInfoEntity.builder()
                .id(updateUserEntity.getId())
                .email(updateUserEntity.getEmail())
                .nickname(updateUserEntity.getNickname())
                .grade("ROLE_" + updateUserEntity.getRole().getGrade())
                .build();
        given(jwtUserInfoRepository.save(jwtUserInfoEntity)).willReturn(jwtUserInfoEntity);
        
        
        // when
        UserEntity resultUserEntity = memberService.updateUserGrade(userGradeUpdateRequest);
        
        
        // then
        assertThat(updateUserEntity).usingRecursiveComparison().isEqualTo(resultUserEntity);
        
        then(userRepository).should(times(1)).findById(userEntity.getId());
        then(roleRepository).should(times(1)).findByGrade(userGradeUpdateRequest.getRole());
        then(userRepository).should(times(1)).save(userEntity);
        then(jwtUserInfoRepository).should(times(1)).save(jwtUserInfoEntity);
    }
    static Stream<Arguments> update_user_grade() {
        return Stream.of(
                Arguments.of(
                        UserGradeUpdateRequest.builder()
                                .id(1L).role(RoleType.TYPE_MEMBER).build(),
                        RoleType.TYPE_MEMBER),
                Arguments.of(
                        UserGradeUpdateRequest.builder()
                                .id(1L).role(RoleType.TYPE_WITHDRAWAL).build(),
                        RoleType.TYPE_WITHDRAWAL)
        );
    }

    
    
    @DisplayName("회원 탈퇴")
    @Test
    void withdrawal() {
        // given
        String jwt = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MiwiZXhwIjoxNzA3NDE3NTc2fQ.3p1pk4Il-lmtq8jlYS5xyKd_78ehPYt-WyVHkN6XrvYq6fGCfnLdRmZrPOvC52nZBcYfGLzz7wUxR8dXOzlQug";
        long userId = 1L;

        // find user entity
        UserEntity userEntity = getUserEntity(userId);
        given(userRepository.findById(userEntity.getId())).willReturn(Optional.of(userEntity));
        
        // find role entity by WITHDRAWAL
        RoleEntity roleEntity = new RoleEntity();
        given(roleRepository.findByGrade(RoleType.TYPE_WITHDRAWAL)).willReturn(roleEntity);
        
        // user entity 탈퇴 권한으로 갱신
        userEntity.setRole(roleEntity);
        userEntity.setWithdrawal(true);
        
        UserEntity updateUserEntity;
        given(userRepository.save(userEntity)).willReturn(updateUserEntity = userEntity);
        
        // redis user 정보 갱신
        JwtUserInfoEntity jwtUserInfoEntity = JwtUserInfoEntity.builder()
                .id(updateUserEntity.getId())
                .email(updateUserEntity.getEmail())
                .nickname(updateUserEntity.getNickname())
                .grade("ROLE_" + updateUserEntity.getRole().getGrade())
                .build();
        given(jwtUserInfoRepository.save(jwtUserInfoEntity)).willReturn(jwtUserInfoEntity);
        
        
        // when
        UserEntity resultUserEntity = memberService.withdrawal(userId);
        
        // then
        assertNotNull(resultUserEntity);
        
        then(userRepository).should(times(1)).findById(userEntity.getId());
        then(roleRepository).should(times(1)).findByGrade(RoleType.TYPE_WITHDRAWAL);
        then(userRepository).should(times(1)).save(userEntity);
        then(jwtUserInfoRepository).should(times(1)).save(jwtUserInfoEntity);
    }
    
    
    
    private UserEntity getUserEntity(
            Long id
    ) {
        return UserEntity.builder()
                .id(id != null ? id : 1L)
                .email("gimwlgus@email.address")
                .role(new RoleEntity())
                .nickname("nickname")
                .password("password")
                .withdrawal(false)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }
}