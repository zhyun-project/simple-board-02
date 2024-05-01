package kim.zhyun.serveruser.config.model;

import kim.zhyun.serveruser.domain.signup.repository.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Builder
public class UserDto {
    
    private Long id;
    private String email;
    private String nickname;
    private String password;
    private boolean withdrawal;
    private RoleEntity role;
    private LocalDateTime modifiedAt;
    


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDto userDto)) return false;
        return Objects.equals(getId(), userDto.getId())
                && Objects.equals(getEmail(), userDto.getEmail())
                && Objects.equals(getNickname(), userDto.getNickname())
                && Objects.equals(getPassword(), userDto.getPassword())
                && Objects.equals(isWithdrawal(), userDto.isWithdrawal())
                && Objects.equals(getRole(), userDto.getRole())
                && Objects.equals(getModifiedAt(), userDto.getModifiedAt());
    }
    
}
