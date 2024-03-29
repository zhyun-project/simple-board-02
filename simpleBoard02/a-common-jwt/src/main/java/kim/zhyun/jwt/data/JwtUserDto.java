package kim.zhyun.jwt.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@JsonInclude(NON_NULL)
public class JwtUserDto {

    private Long id;
    private String email;
    private String nickname;

    /**  @apiNote ⚠️ `Authentication.principal` 객체에만 사용  */
    public static JwtUserDto from(Object principal) {
        
        if (principal instanceof JwtUserInfo source) {
            return JwtUserDto.builder()
                    .id(source.getId())
                    .email(source.getEmail())
                    .nickname(source.getNickname()).build();
        }
        
        return (JwtUserDto) principal;
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JwtUserDto userDto)) return false;
        return Objects.equals(getId(), userDto.getId())
                && Objects.equals(getEmail(), userDto.getEmail())
                && Objects.equals(getNickname(), userDto.getNickname());
    }
    
}
