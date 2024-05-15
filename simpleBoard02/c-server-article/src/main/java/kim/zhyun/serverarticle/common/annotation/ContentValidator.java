package kim.zhyun.serverarticle.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentValidator implements ConstraintValidator<Content, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        
        if (value == null)
            return false;

        String regex = "^(?! )(?=\\S)(.{1,})(?<=\\S)(?<! )$";
        Matcher patternMatcher = Pattern.compile(regex).matcher(value);
        
        return patternMatcher.matches();
    }
    
}
