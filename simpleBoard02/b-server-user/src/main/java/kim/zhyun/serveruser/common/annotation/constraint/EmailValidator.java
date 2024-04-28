package kim.zhyun.serveruser.common.annotation.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kim.zhyun.serveruser.common.advice.ApiException;
import kim.zhyun.serveruser.common.annotation.Email;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<Email, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        
        if (value != null) {
            String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
            Matcher patternMatcher = Pattern.compile(regex).matcher(value);
            
            return patternMatcher.matches();
        }
        
        return true;
    }
    
}
