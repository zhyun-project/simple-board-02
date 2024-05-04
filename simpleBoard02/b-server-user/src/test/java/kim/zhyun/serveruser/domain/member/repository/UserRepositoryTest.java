package kim.zhyun.serveruser.domain.member.repository;

import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
import kim.zhyun.serveruser.domain.signup.repository.RoleRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("users entity CRUD 테스트")
@SpringBootTest
class UserRepositoryTest {
    @Autowired private UserRepository userRepository;
    
    // 생성자(맨 아래 위치)에서 할당
    private final RoleEntity roleMember;
    private final RoleEntity roleWithdrawal;

    private final String mail = "email@email.com";
    private final String nickname = "nickname";
    private final String password = "password";
    private final boolean withdrawal = false;
    
    
    @BeforeEach public void beforeEach() {
        var newUserEntity = userEntityBuilder(
                mail,
                nickname,
                password,
                roleMember,
                withdrawal
        );
        userRepository.save(newUserEntity);
    }
    
    @AfterEach public void afterEach() {
        userRepository.deleteAll();
    }
    
    
    @DisplayName("저장 - 성공")
    @Test
    void user_entity_save_success() {
        String saveTest = "saveTest_%s";
        
        // given
        UserEntity requestUserEntity = userEntityBuilder(
                saveTest.formatted(mail),
                saveTest.formatted(nickname),
                saveTest.formatted(password),
                roleMember,
                withdrawal
        );
        
        // when
        UserEntity savedUserEntity = userRepository.save(requestUserEntity);
        
        // then
        requestUserEntity.setId(savedUserEntity.getId());
        assertThat(savedUserEntity).isEqualTo(requestUserEntity);
    }
    
    
    @DisplayName("저장 실패")
    @ParameterizedTest(name = "null : {0}")
    @ValueSource(strings = {
            "email", "nickname", "password", "role"
    })
    void user_entity_save_fail(
            String nullField
    ) {
        String saveTest = "saveTest_%s";
        
        // then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.save(userEntityBuilder(
                        nullField.equals("email")    ? null : saveTest.formatted(mail),
                        nullField.equals("nickname") ? null : saveTest.formatted(nickname),
                        nullField.equals("password") ? null : saveTest.formatted(password),
                        nullField.equals("role")     ? null : roleMember,
                        false
                ))
        );
    }
    
    @DisplayName("읽기 성공")
    @Test
    void user_entity_read_success() {
        
        // given-when
        UserEntity userEntity = userRepository.save(userEntityBuilder(
                mail,
                nickname,
                password,
                roleMember,
                withdrawal
        ));
        
        // then
        assertThat(userEntity).isNotNull();
        assertAll(
                () -> assertNotNull(userEntity.getCreatedAt()),
                () -> assertNotNull(userEntity.getModifiedAt()),
                () -> assertEquals(userEntity.getEmail(), mail),
                () -> assertEquals(userEntity.getPassword(), password),
                () -> assertEquals(userEntity.getNickname(), nickname),
                () -> assertEquals(userEntity.getRole(), roleMember),
                () -> assertEquals(userEntity.isWithdrawal(), withdrawal)
        );
    }


    @DisplayName("수정 - 성공")
    @Test
    void user_entity_update_success() {
        // given
        UserEntity requestUserEntity = userRepository.findAll().get(0);
        
        requestUserEntity.setEmail("update@update.update");
        requestUserEntity.setPassword("udt password");
        requestUserEntity.setNickname("udt nickname");
        requestUserEntity.setRole(roleWithdrawal);
        requestUserEntity.setWithdrawal(!requestUserEntity.isWithdrawal());
        
        // when
        UserEntity originUserEntity = userRepository.findById(requestUserEntity.getId()).get();
        UserEntity updatedUserEntity = userRepository.save(requestUserEntity);
        
        // then
        assertAll(
                () -> assertEquals(updatedUserEntity.getCreatedAt(), originUserEntity.getCreatedAt()),
                
                () -> assertNotEquals(updatedUserEntity.getModifiedAt(),originUserEntity.getModifiedAt()),
                () -> assertNotEquals(updatedUserEntity.getEmail(),     originUserEntity.getEmail()),
                () -> assertNotEquals(updatedUserEntity.getPassword(),  originUserEntity.getPassword()),
                () -> assertNotEquals(updatedUserEntity.getNickname(),  originUserEntity.getNickname()),
                () -> assertNotEquals(updatedUserEntity.getRole(),      originUserEntity.getRole()),
                () -> assertNotEquals(updatedUserEntity.isWithdrawal(), originUserEntity.isWithdrawal())
        );
    }
    

    @DisplayName("수정 - 실패")
    @ParameterizedTest(name = "null : {0}")
    @ValueSource(strings = {
            "email", "password", "nickname", "role"
    })
    void user_entity_update_fail(String nullField) {
        UserEntity userEntity = userRepository.findAll().get(0);

        userEntity.setEmail(nullField.equals("email") ? null : "gimwlgus@gmail.com");
        userEntity.setPassword(nullField.equals("password") ? null : "udt password");
        userEntity.setNickname(nullField.equals("nickname") ? null : "udt nickname");
        userEntity.setRole(nullField.equals("role") ? null : roleWithdrawal);
        userEntity.setWithdrawal(!userEntity.isWithdrawal());

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.save(userEntity)
        );
    }

    
    @DisplayName("삭제 - 성공")
    @Test
    void user_entity_delete_success() {
        // given
        UserEntity requestUserEntity = userRepository.findAll().get(0);
        
        // when
        userRepository.delete(requestUserEntity);
        
        // then
        Optional<UserEntity> optionalUserEntity = userRepository.findById(requestUserEntity.getId());
        
        assertThat(optionalUserEntity).isEmpty();
    }
    

    
    // user entity 생성
    UserEntity userEntityBuilder(
            String email, String nickname, String password, RoleEntity role, boolean isWithdrawal
    ) {
        return UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .withdrawal(isWithdrawal)
                .role(role)
                .password(password)
                
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }
    
    UserRepositoryTest(
            @Autowired RoleRepository roleRepository
    ) {
        roleMember = roleRepository.findByGrade(RoleType.TYPE_MEMBER);
        roleWithdrawal = roleRepository.findByGrade(RoleType.TYPE_WITHDRAWAL);
    }
}

