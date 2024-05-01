package kim.zhyun.serveruser.domain.member.repository;

import kim.zhyun.jwt.common.constants.type.RoleType;
import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
import kim.zhyun.serveruser.domain.signup.repository.RoleRepository;
import org.junit.jupiter.api.*;
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
    
    
    @DisplayName("users entity 저장 테스트")
    @Nested
    class TestSave {
        
        @DisplayName("성공")
        @Test
        void user_entity_save_success() {
            // given
            UserEntity requestUserEntity = userEntityBuilder(
                    "gimwlgus@gmail.com",
                    "ergus",
                    "1234",
                    roleMember,
                    false
            );
            
            // when
            UserEntity savedUserEntity = userRepository.save(requestUserEntity);
            
            // then
            requestUserEntity.setId(savedUserEntity.getId());
            assertThat(savedUserEntity).isEqualTo(requestUserEntity);
        }
        

        @DisplayName("실패 case 실행 메서드")
        private void runOnFail(UserEntity requestUserEntity) {
            // then
            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> userRepository.save(requestUserEntity)
            );
        }
        
        @DisplayName("실패 case 모음")
        @Nested
        class FailCases {
            @DisplayName("실패 - email null")
            @Test
            void user_entity_save_fail_null_in_email() {
                UserEntity userEntity = userEntityBuilder(
                        null,
                        "ergus",
                        "1234",
                        roleMember,
                        false
                );
                
                runOnFail(userEntity);
            }
            
            @DisplayName("실패 - nickname null")
            @Test
            void user_entity_save_fail_null_in_nickname() {
                UserEntity userEntity = userEntityBuilder(
                        "gimwlgus@gmail.com",
                        null,
                        "1234",
                        roleMember,
                        false
                );
                
                runOnFail(userEntity);
            }
            
            @DisplayName("실패 - password null")
            @Test
            void user_entity_save_fail_null_in_password() {
                UserEntity userEntity = userEntityBuilder(
                        "gimwlgus@gmail.com",
                        "ergus",
                        null,
                        roleMember,
                        false
                );
                
                runOnFail(userEntity);
            }
            
            @DisplayName("실패 - role null")
            @Test
            void user_entity_save_fail_null_in_role() {
                UserEntity userEntity = userEntityBuilder(
                        "gimwlgus@gmail.com",
                        "ergus",
                        "1234",
                        null,
                        false
                );
                
                runOnFail(userEntity);
            }
        }
    }
    
    
    @DisplayName("users entity 읽기 테스트")
    @Nested
    class TestRead {
        @AfterEach public void afterEach() {
            userRepository.deleteAll();
        }
        
        @DisplayName("성공")
        @Test
        void user_entity_read_success() {
            String mail = "email@email.com";
            String nickname = "nickname";
            String password = "password";
            RoleEntity role = roleMember;
            boolean withdrawal = false;
            
            // given-when
            UserEntity userEntity = userRepository.save(userEntityBuilder(
                    mail,
                    nickname,
                    password,
                    role,
                    withdrawal
            ));
            
            // then
            assertThat(userEntity).isNotNull();
            assertThat(userEntity.getCreatedAt()) .isNotNull();
            assertThat(userEntity.getModifiedAt()).isNotNull();
            assertThat(userEntity.getEmail())     .isEqualTo(mail);
            assertThat(userEntity.getPassword())  .isEqualTo(password);
            assertThat(userEntity.getNickname())  .isEqualTo(nickname);
            assertThat(userEntity.getRole())      .isEqualTo(role);
            assertThat(userEntity.isWithdrawal()) .isEqualTo(withdrawal);
        }
    }
    
    
    @DisplayName("users entity 수정 테스트")
    @Nested
    class TestUpdate {
        
        @BeforeEach public void beforeEach() {
            var newUserEntity = userEntityBuilder(
                    "email@email.com",
                    "nickname",
                    "password",
                    roleMember,
                    false
            );
            userRepository.save(newUserEntity);
        }
        @AfterEach public void afterEach() {
            userRepository.deleteAll();
        }
        
        
        
        @DisplayName("성공")
        @Test
        void user_entity_update_success() {
            // given
            UserEntity requestUserEntity = userRepository.findAll().get(0);
            
            requestUserEntity.setEmail("update@update.update");
            requestUserEntity.setPassword("udt password");
            requestUserEntity.setNickname("udt nickname");
            requestUserEntity.setRole(roleWithdrawal);
            requestUserEntity.setWithdrawal(true);
            
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
        
        
        // 실패 케이스 실행
        @DisplayName("실패 case 실행 메서드")
        private void runOnFail(UserEntity requestUserEntity) {
            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> userRepository.save(requestUserEntity)
            );
        }
        
        @DisplayName("실패 case 모음")
        @Nested
        class FailNested {
            
            @DisplayName("실패 - email null")
            @Test
            void user_entity_update_fail_null_in_email() {
                UserEntity userEntity = userRepository.findAll().get(0);
                
                userEntity.setEmail(null);
                userEntity.setPassword("udt password");
                userEntity.setNickname("udt nickname");
                userEntity.setRole(roleWithdrawal);
                userEntity.setWithdrawal(true);
                
                runOnFail(userEntity);
            }
            
            @DisplayName("실패 - nickname null")
            @Test
            void user_entity_update_fail_null_in_nickname() {
                UserEntity userEntity = userRepository.findAll().get(0);
                
                userEntity.setEmail("update@update.update");
                userEntity.setPassword("udt password");
                userEntity.setNickname(null);
                userEntity.setRole(roleWithdrawal);
                userEntity.setWithdrawal(true);
                
                runOnFail(userEntity);
            }
            
            @DisplayName("실패 - password null")
            @Test
            void user_entity_update_fail_null_in_password() {
                UserEntity userEntity = userRepository.findAll().get(0);
                
                userEntity.setEmail("update@update.update");
                userEntity.setPassword(null);
                userEntity.setNickname("udt nickname");
                userEntity.setRole(roleWithdrawal);
                userEntity.setWithdrawal(true);
                
                runOnFail(userEntity);
            }
            
            @DisplayName("실패 - role null")
            @Test
            void user_entity_update_fail_null_in_role() {
                UserEntity userEntity = userRepository.findAll().get(0);
                
                userEntity.setEmail("update@update.update");
                userEntity.setPassword("udt password");
                userEntity.setNickname("udt nickname");
                userEntity.setRole(null);
                userEntity.setWithdrawal(true);
                
                runOnFail(userEntity);
            }
        }
    }
    
    
    @DisplayName("users entity 삭제 테스트")
    @Nested
    class TestDelete {
        
        @BeforeEach public void beforeEach() {
            var newUserEntity = userEntityBuilder(
                    "email@email.com",
                    "nickname",
                    "password",
                    roleMember,
                    false
            );
            userRepository.save(newUserEntity);
        }
        @AfterEach public void afterEach() {
            userRepository.deleteAll();
        }
        
        
        @DisplayName("성공")
        @Test
        void user_entity_update_success() {
            // given
            UserEntity requestUserEntity = userRepository.findAll().get(0);
            
            // when
            userRepository.delete(requestUserEntity);
            
            // then
            Optional<UserEntity> optionalUserEntity = userRepository.findById(requestUserEntity.getId());
            
            assertThat(optionalUserEntity).isEmpty();
        }
    }
    
    
    
    
    
    private UserEntity userEntityBuilder(
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
    
    public UserRepositoryTest(
            @Autowired RoleRepository roleRepository
    ) {
        roleMember = roleRepository.findByGrade(RoleType.TYPE_MEMBER);
        roleWithdrawal = roleRepository.findByGrade(RoleType.TYPE_WITHDRAWAL);
    }
}