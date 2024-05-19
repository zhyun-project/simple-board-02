package kim.zhyun.serveruser.common.value;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SignUpValue {
    
    public List<String> adminEmails;

    
    
    public SignUpValue(
            @Value("${sign-up.admin}") List<String> adminEmails
    ) {
        this.adminEmails = adminEmails;
    }
}
