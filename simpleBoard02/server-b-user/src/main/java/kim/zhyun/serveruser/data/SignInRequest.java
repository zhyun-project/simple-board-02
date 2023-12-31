package kim.zhyun.serveruser.data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kim.zhyun.serveruser.data.message.ExceptionMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.VALID_PASSWORD_EXCEPTION_MESSAGE;

@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SignInRequest {
    
    @Email(message = ExceptionMessage.VALID_EMAIL_EXCEPTION_MESSAGE, regexp = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$")
    @NotNull
    private String email;
    
    @Size(min = 4, message = VALID_PASSWORD_EXCEPTION_MESSAGE)
    private String password;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SignInRequest that)) return false;
        
        return Objects.equals(email, that.email) && Objects.equals(password, that.password);
    }
    
}
