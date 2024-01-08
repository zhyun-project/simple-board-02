package kim.zhyun.serveruser.data.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kim.zhyun.serveruser.data.entity.Role;
import kim.zhyun.serveruser.data.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Builder
@JsonInclude(NON_NULL)
public class UserResponse {
    
    private Long id;
    private String email;
    private String nickname;
    private Role role;
    
    public static UserResponse from(User source) {
        return UserResponse.builder()
                .id(source.getId())
                .email(source.getEmail())
                .nickname(source.getNickname())
                .role(source.getRole()).build();
    }
    
    public static User to(UserResponse dto) {
        return User.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .nickname(dto.getEmail())
                .role(dto.getRole()).build();
    }
    
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
