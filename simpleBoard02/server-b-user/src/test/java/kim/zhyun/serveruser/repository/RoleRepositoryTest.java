package kim.zhyun.serveruser.repository;

import kim.zhyun.serveruser.config.JpaConfig;
import kim.zhyun.serveruser.data.type.RoleType;
import kim.zhyun.serveruser.data.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        String roleName = RoleType.ADMIN.name();
        
        // when
        Optional<Role> role = roleRepository.findByRoleIgnoreCase(roleName);
        
        // then
        assertTrue(role.isPresent());
    }
    
    @DisplayName("role 검색 : findByRole - 성공 (대소문자 섞인 검색어)")
    @Test
    void find_role_true_ignore_case() {
        // given
        String roleName = "AdmiN";
        
        // when
        Optional<Role> role = roleRepository.findByRoleIgnoreCase(roleName);
        
        // then
        assertTrue(role.isPresent());
    }
    
    @DisplayName("role 검색 : findByRole - 없는 값 검색")
    @Test
    void find_role_null() {
        // given
        String roleName = "ADMINISTRATOR";
        
        // when
        Optional<Role> role = roleRepository.findByRoleIgnoreCase(roleName);
        
        // then
        assertTrue(role.isEmpty());
    }
    
    @DisplayName("role 검색 : existByRole - true")
    @Test
    void exist_role_true() {
        // given
        String role = RoleType.ADMIN.name();
        
        // when
        boolean exists = roleRepository.existsByRole(role);
        
        // then
        assertTrue(exists);
    }
    
    @DisplayName("role 검색 : existByRole - false")
    @Test
    void exist_role_false() {
        // given
        String role = "ADMINISTRATOR";
        
        // when
        boolean exists = roleRepository.existsByRole(role);
        
        // then
        assertThat(exists).isFalse();
    }
    
}
