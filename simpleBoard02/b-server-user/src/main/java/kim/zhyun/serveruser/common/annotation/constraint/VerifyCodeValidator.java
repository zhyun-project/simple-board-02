package kim.zhyun.serveruser.common.annotation.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kim.zhyun.serveruser.common.annotation.VerifyCode;
import org.apache.logging.log4j.util.Strings;

public class VerifyCodeValidator implements ConstraintValidator<VerifyCode, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        
        return Strings.isNotBlank(value);
    }
    
}
