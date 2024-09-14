package kim.zhyun.jwt.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.Objects;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class JwtUserInfoDto {

    private Long id;

    @JsonIgnore
    private String email;

    private String nickname;
    private String grade;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JwtUserInfoDto userDto)) return false;
        return Objects.equals(getId(), userDto.getId())
                && Objects.equals(getEmail(), userDto.getEmail())
                && Objects.equals(getNickname(), userDto.getNickname())
                && Objects.equals(getGrade(), userDto.getGrade());
    }
    
}
