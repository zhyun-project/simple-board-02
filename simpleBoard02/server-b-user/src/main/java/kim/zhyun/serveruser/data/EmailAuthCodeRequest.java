package kim.zhyun.serveruser.data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_VALID_EMAIL_FORMAT;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
public class EmailAuthCodeRequest {
    
    @NotBlank(message = EXCEPTION_VALID_EMAIL_FORMAT)
    @Email(message = EXCEPTION_VALID_EMAIL_FORMAT, regexp = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$")
    private String email;
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        EmailAuthCodeRequest that = (EmailAuthCodeRequest) obj;
        
        return Objects.equals(email, that.email);
    }
}
