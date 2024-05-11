package kim.zhyun.serveruser.domain.signup.service;

import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.jwt.domain.repository.JwtUserInfoRepository;
import kim.zhyun.serveruser.common.value.SignUpValue;
import kim.zhyun.serveruser.domain.member.repository.UserEntity;
import kim.zhyun.serveruser.domain.member.repository.UserRepository;
import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
import kim.zhyun.serveruser.domain.signup.repository.RoleRepository;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {
    
    @Mock RoleRepository roleRepository;
    @Mock UserRepository userRepository;
    @Mock JwtUserInfoRepository jwtUserInfoRepository;
    
    SignUpService signUpService;
    List<String> adminEmails = List.of(
            "admin@email.mail",
            "admin@mail.ail",
            "admin@gmail.com",
            "admin@email.address"
    );
    
    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.openMocks(this);
        signUpService = new SignUpService(
                roleRepository,
                userRepository,
                jwtUserInfoRepository,
                new SignUpValue(adminEmails)
        );
    }
    
    @DisplayName("사용 가능한 이메일인지 여부 반환")
    @ParameterizedTest(name = "사용 중: {0}")
    @ValueSource(booleans = {true, false})
    void isAvailableEmail(boolean usedEmail) {
        String requestEmail = "email@mail.ail";
        
        given(userRepository.existsByEmailIgnoreCase(requestEmail)).willReturn(usedEmail);
        
        // when
        boolean isAvailableEmail = signUpService.isAvailableEmail(requestEmail);
        
        // then
        assertNotEquals(usedEmail, isAvailableEmail);
        
        then(userRepository).should(times(1)).existsByEmailIgnoreCase(requestEmail);
    }
    
    @DisplayName("사용중인 닉네임인지 여부 반환")
    @ParameterizedTest(name = "사용 중: {0}")
    @ValueSource(booleans = {true, false})
    void isUsedNickname(boolean usedNickname) {
        String requestNickname = "nickname";
        
        given(userRepository.existsByNicknameIgnoreCase(requestNickname)).willReturn(usedNickname);
        
        // when
        boolean isUsedNickname = signUpService.isUsedNickname(requestNickname);
        
        // then
        assertEquals(usedNickname, isUsedNickname);
        
        then(userRepository).should(times(1)).existsByNicknameIgnoreCase(requestNickname);
    }
    
    @DisplayName("이메일로 등급(권한) 조회")
    @ParameterizedTest(name = "email: {0}")
    @ValueSource(strings = {
            "admin@email.mail",
            "member@email.mail"
    })
    void getGrade(String requestEmail) {
        String admins = Strings.join(adminEmails, ',');
        String grade = admins == null || !admins.contains(requestEmail)
                ? RoleType.TYPE_MEMBER
                : RoleType.TYPE_ADMIN;
        
        given(roleRepository.findByGrade(grade)).willReturn(new RoleEntity());
        
        
        // when
        signUpService.getGrade(requestEmail);
        
        
        // then
        assertEquals(
                requestEmail.equals("admin@email.mail")
                        ? RoleType.TYPE_ADMIN : RoleType.TYPE_MEMBER,
                grade
        );
        
        then(roleRepository).should(times(1)).findByGrade(grade);
    }
    
    @DisplayName("멤버 DB에 신규 저장")
    @Test
    void saveUser() {
        UserEntity newUserEntity = UserEntity.builder()
                .email("member@email.mail")
                .password("password")
                .nickname("nickname")
                .role(new RoleEntity())
                .withdrawal(false)
                .build();
        
        UserEntity savedNewUserEntity;
        
        given(userRepository.save(newUserEntity)).willReturn(savedNewUserEntity = UserEntity.builder()
                .email(newUserEntity.getEmail())
                .password(newUserEntity.getPassword())
                .nickname(newUserEntity.getNickname())
                .role(newUserEntity.getRole())
                .withdrawal(newUserEntity.isWithdrawal())
                
                .id(100L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());

        
        // when
        UserEntity savedUserEntity = signUpService.saveUser(newUserEntity);
        
        
        // then
        assertEquals(savedNewUserEntity, savedUserEntity);
        assertNotEquals(newUserEntity, savedNewUserEntity);
        
        then(userRepository).should(times(1)).save(newUserEntity);
    }
    
    @DisplayName("redis user info 업데이트")
    @Test
    void jwtUserInfoUpdate() {
        UserEntity requestUserEntity = UserEntity.builder()
                .email("member@email.mail")
                .password("password")
                .nickname("nickname")
                .role(new RoleEntity())
                .withdrawal(false)
                
                .id(100L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
        
        JwtUserInfoEntity newJwtUserEntity = JwtUserInfoEntity.builder()
                .id(requestUserEntity.getId())
                .email(requestUserEntity.getEmail())
                .nickname(requestUserEntity.getNickname())
                .grade("ROLE_" + requestUserEntity.getRole().getGrade())
                .build();
        
        JwtUserInfoEntity savedNewJwtUserEntity;
        
        given(jwtUserInfoRepository.save(newJwtUserEntity)).willReturn(savedNewJwtUserEntity = newJwtUserEntity);
        
        
        // when
        signUpService.jwtUserInfoUpdate(requestUserEntity);
        
        
        // then
        then(jwtUserInfoRepository).should(times(1)).save(newJwtUserEntity);
    }
    
}

