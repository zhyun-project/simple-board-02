package kim.zhyun.serveruser.common.annotation.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kim.zhyun.serveruser.common.annotation.Nickname;

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
