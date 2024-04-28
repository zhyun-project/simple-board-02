package kim.zhyun.serveruser.domain.signup.controller.model;

import kim.zhyun.serveruser.common.annotation.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
public class EmailAuthCodeRequest {
    
    @Email
    private String email;
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        EmailAuthCodeRequest that = (EmailAuthCodeRequest) obj;
        
        return Objects.equals(email, that.email);
    }
}
