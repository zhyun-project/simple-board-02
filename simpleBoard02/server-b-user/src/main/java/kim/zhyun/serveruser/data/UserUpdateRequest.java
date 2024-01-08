package kim.zhyun.serveruser.data;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_VALID_NICKNAME_FORMAT;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_VALID_PASSWORD_FORMAT;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class UserUpdateRequest {
    private long id;
    private String email;

    @Length(min = 1, max = 6, message = EXCEPTION_VALID_NICKNAME_FORMAT)
    private String nickname;
    
    @Size(min = 4, message = EXCEPTION_VALID_PASSWORD_FORMAT)
    private String password;
}
