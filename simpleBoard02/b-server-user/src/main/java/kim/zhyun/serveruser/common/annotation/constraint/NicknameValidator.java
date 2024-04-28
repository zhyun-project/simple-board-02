package kim.zhyun.serveruser.common.annotation.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kim.zhyun.serveruser.common.advice.ApiException;
import kim.zhyun.serveruser.common.annotation.Nickname;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

public class NicknameValidator implements ConstraintValidator<Nickname, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        
        if (value != null) {
            int length = value.length();

            return length >= 1 && length <= 6;
        }
        
        return true;
    }
    
}
