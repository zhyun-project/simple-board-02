package kim.zhyun.serveruser.common.annotation.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kim.zhyun.serveruser.common.annotation.Email;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<Email, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        
        if (value != null) {
            String regex = "^[a-zA-Z0-9][-_.+a-zA-Z0-9]*@[a-zA-Z0-9](?:[-_.a-zA-Z0-9]*\\.[a-zA-Z0-9]+)+$";
            Matcher patternMatcher = Pattern.compile(regex).matcher(value);
            
            return patternMatcher.matches();
        }
        
        return true;
    }
    
}
