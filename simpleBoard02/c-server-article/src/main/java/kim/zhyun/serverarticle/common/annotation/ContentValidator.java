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

//        String regex = "\\S+"; // 공백이 아닌 문자가 1개 이상 있어야 됨
//        Matcher patternMatcher = Pattern.compile(regex).matcher(value);
//
//        return patternMatcher.matches();
        return true;
    }
    
}
