package kim.zhyun.serveruser.data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.VALID_PASSWORD_EXCEPTION_MESSAGE;

@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class SignupRequest {
    
    @NotNull private String email;
    @NotNull private String nickname;
    
    @NotNull @Size(min = 4, message = VALID_PASSWORD_EXCEPTION_MESSAGE)
    private String password;
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SignupRequest that)) return false;
        
        return Objects.equals(email, that.email) && Objects.equals(nickname, that.nickname) && Objects.equals(password, that.password);
    }
    
}
