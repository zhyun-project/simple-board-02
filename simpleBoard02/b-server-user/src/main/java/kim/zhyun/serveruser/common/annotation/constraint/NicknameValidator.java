package kim.zhyun.serveruser.common.annotation.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kim.zhyun.serveruser.common.annotation.Nickname;
import org.apache.logging.log4j.util.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NicknameValidator implements ConstraintValidator<Nickname, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        
        if (value != null) {
            String regex = "^(?! )(?=\\S)(.{1,6})(?<=\\S)(?<! )$";
            Matcher patternMatcher = Pattern.compile(regex).matcher(value);
            
            return patternMatcher.matches();
        }
        
        return true;
    }
    
}
