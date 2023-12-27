package kim.zhyun.serveruser.data;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
public class EmailAuthCodeRequest {
    
    @Email(message = "올바른 이메일 주소를 입력해주세요.", regexp = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$")
    private String email;
    
}
