package kim.zhyun.serveruser.data;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class UserDto {
    
    private Long id;
    private String email;
    private String nickname;
    private String password;
    
    public static UserDto from(User source) {
        return UserDto.builder()
                .id(source.getId())
                .email(source.getEmail())
                .nickname(source.getEmail()).build();
    }
    
    public static User to(UserDto dto) {
        return User.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .nickname(dto.getEmail())
                .password(dto.getPassword()).build();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDto userDto)) return false;
        return Objects.equals(getId(), userDto.getId())
                && Objects.equals(getEmail(), userDto.getEmail())
                && Objects.equals(getNickname(), userDto.getNickname())
                && Objects.equals(getPassword(), userDto.getPassword());
    }
}
