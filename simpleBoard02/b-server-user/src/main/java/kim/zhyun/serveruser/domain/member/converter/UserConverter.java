package kim.zhyun.serveruser.domain.member.converter;

import kim.zhyun.serveruser.config.model.UserDto;
import kim.zhyun.serveruser.domain.member.controller.model.UserResponse;
import kim.zhyun.serveruser.domain.member.repository.UserEntity;
import kim.zhyun.serveruser.domain.signup.controller.model.SignupRequest;
import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserConverter {
    private final PasswordEncoder passwordEncoder;
    
    public UserResponse toResponse(UserEntity source) {
        return UserResponse.builder()
                .id(source.getId())
                .email(source.getEmail())
                .nickname(source.getNickname())
                .role(source.getRole()).build();
    }
    
    public UserDto toDto(UserEntity source) {
        return UserDto.builder()
                .id(source.getId())
                .email(source.getEmail())
                .password(source.getPassword())
                .nickname(source.getNickname())
                .withdrawal(source.isWithdrawal())
                .role(source.getRole())
                .modifiedAt(source.getModifiedAt()).build();
    }
    
    public UserEntity toEntity(SignupRequest request, RoleEntity roleEntity) {
        return UserEntity.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .roleEntity(roleEntity)
                .build();
    }
    
}
