package kim.zhyun.serverarticle.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ContentValidator implements ConstraintValidator<Content, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {

        return value != null && !value.trim().isEmpty();
        /*
        if (value == null)
            return false;
        
        String regex = "(?s).*\\S.*";
        
        return Pattern.compile(regex).matcher(value).matches();
        */
    }
    
}
