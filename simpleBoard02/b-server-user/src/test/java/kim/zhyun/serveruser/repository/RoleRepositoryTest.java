package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.config.JpaConfig;
import kim.zhyun.serveruser.domain.signup.repository.Role;
import kim.zhyun.serveruser.domain.signup.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static kim.zhyun.serveruser.common.model.type.RoleType.TYPE_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Role DB Test")
@Transactional
@Import(JpaConfig.class)
@SpringBootTest
class RoleRepositoryTest extends PrintLog<RoleRepository> {
    private final RoleRepository roleRepository;
    public RoleRepositoryTest(@Autowired RoleRepository roleRepository) {
        super(roleRepository);
        this.roleRepository = roleRepository;
    }
    
    @DisplayName("전체 데이터 조회")
    @Test
    void find_all() { }
    
    @DisplayName("role 검색 : findByRole - 성공")
    @Test
    void find_role_true() {
        // given
        String roleName = TYPE_ADMIN;
        
        // when
        Role role = roleRepository.findByGrade(roleName);
        
        // then
        assertThat(role.getGrade()).isEqualToIgnoringCase(roleName);
    }
    
    
    @DisplayName("role 검색 : findByRole - 없는 값 검색")
    @Test
    void find_role_null() {
        // given
        String roleName = "ADMINISTRATOR";
        
        // when
        Role role = roleRepository.findByGrade(roleName);
        
        // then
        assertNull(role);
    }
    
    @DisplayName("role 검색 : existByRole - true")
    @Test
    void exist_role_true() {
        // given
        String role = TYPE_ADMIN;
        
        // when
        boolean exists = roleRepository.existsByGrade(role);
        
        // then
        assertTrue(exists);
    }
    
    @DisplayName("role 검색 : existByRole - false")
    @Test
    void exist_role_false() {
        // given
        String role = "ADMINISTRATOR";
        
        // when
        boolean exists = roleRepository.existsByGrade(role);
        
        // then
        assertThat(exists).isFalse();
    }
    
}
