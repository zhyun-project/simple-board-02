package kim.zhyun.serveruser.domain.member.controller.model;

import kim.zhyun.serveruser.domain.signup.repository.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Builder
public class UserResponse {
    
    private Long id;
    private String email;
    private String nickname;
    private Role role;
    
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserResponse userDto)) return false;
        return Objects.equals(getId(), userDto.getId())
                && Objects.equals(getEmail(), userDto.getEmail())
                && Objects.equals(getNickname(), userDto.getNickname())
                && Objects.equals(getRole(), userDto.getRole());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEmail(), getNickname(), getRole());
    }
    
}
