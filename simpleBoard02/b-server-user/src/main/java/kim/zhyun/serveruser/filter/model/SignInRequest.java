package kim.zhyun.serveruser.filter.model;

import jakarta.validation.constraints.Size;
import kim.zhyun.serveruser.common.annotation.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_VALID_PASSWORD_FORMAT;

@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SignInRequest {
    
    @Email
    private String email;
    
    @Size(min = 4, message = EXCEPTION_VALID_PASSWORD_FORMAT)
    private String password;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SignInRequest that)) return false;
        
        return Objects.equals(email, that.email) && Objects.equals(password, that.password);
    }
    
}
