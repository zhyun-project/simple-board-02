package kim.zhyun.serveruser.domain.member.repository;

import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
import kim.zhyun.serveruser.domain.signup.repository.RoleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        // given
        UserEntity requestUserEntity = userEntityBuilder(
                mail,
                nickname,
                password,
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
        // then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.save(userEntityBuilder(
                        nullField.equals("email")    ? null : "zhyun@gmail.com",
                        nullField.equals("nickname") ? null : "ergus",
                        nullField.equals("password") ? null : "1234",
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
        assertThat(userEntity.getCreatedAt()) .isNotNull();
        assertThat(userEntity.getModifiedAt()).isNotNull();
        assertThat(userEntity.getEmail())     .isEqualTo(mail);
        assertThat(userEntity.getPassword())  .isEqualTo(password);
        assertThat(userEntity.getNickname())  .isEqualTo(nickname);
        assertThat(userEntity.getRole())      .isEqualTo(roleMember);
        assertThat(userEntity.isWithdrawal()) .isEqualTo(withdrawal);
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
        assertThat(updatedUserEntity.getCreatedAt()) .isEqualTo(originUserEntity.getCreatedAt());
        
        assertThat(updatedUserEntity.getModifiedAt()).isNotEqualTo(originUserEntity.getModifiedAt());
        assertThat(updatedUserEntity.getEmail())     .isNotEqualTo(originUserEntity.getEmail());
        assertThat(updatedUserEntity.getPassword())  .isNotEqualTo(originUserEntity.getPassword());
        assertThat(updatedUserEntity.getNickname())  .isNotEqualTo(originUserEntity.getNickname());
        assertThat(updatedUserEntity.getRole())      .isNotEqualTo(originUserEntity.getRole());
        assertThat(updatedUserEntity.isWithdrawal()) .isNotEqualTo(originUserEntity.isWithdrawal());
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

