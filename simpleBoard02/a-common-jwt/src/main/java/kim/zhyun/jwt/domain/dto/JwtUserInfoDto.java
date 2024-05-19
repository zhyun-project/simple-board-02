package kim.zhyun.jwt.domain.dto;

import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import lombok.*;

import java.util.Objects;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class JwtUserInfoDto {

    private Long id;
    private String email;
    private String nickname;

    /**  @apiNote ⚠️ `Authentication.principal` 객체에만 사용  */
    public static JwtUserInfoDto from(Object principal) {
        
        if (principal instanceof JwtUserInfoEntity source) {
            return JwtUserInfoDto.builder()
                    .id(source.getId())
                    .email(source.getEmail())
                    .nickname(source.getNickname()).build();
        }
        
        return (JwtUserInfoDto) principal;
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JwtUserInfoDto userDto)) return false;
        return Objects.equals(getId(), userDto.getId())
                && Objects.equals(getEmail(), userDto.getEmail())
                && Objects.equals(getNickname(), userDto.getNickname());
    }
    
}
