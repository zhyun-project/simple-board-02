package kim.zhyun.serveruser.domain.signup.controller.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kim.zhyun.serveruser.common.annotation.Email;
import kim.zhyun.serveruser.common.annotation.Nickname;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_VALID_PASSWORD_FORMAT;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class SignupRequest {
    
    @Email private String email;
    @Nickname private String nickname;
    
    @NotNull @Size(min = 4, message = EXCEPTION_VALID_PASSWORD_FORMAT)
    private String password;
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SignupRequest that)) return false;
        
        return Objects.equals(email, that.email) && Objects.equals(nickname, that.nickname) && Objects.equals(password, that.password);
    }
    
}
