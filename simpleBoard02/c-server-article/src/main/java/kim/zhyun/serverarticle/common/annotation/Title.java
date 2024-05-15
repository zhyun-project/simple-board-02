package kim.zhyun.serverarticle.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static kim.zhyun.serverarticle.common.message.ExceptionMessage.EXCEPTION_TITLE_FORMAT;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TitleValidator.class)
public @interface Title {
    
    String message() default EXCEPTION_TITLE_FORMAT;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
}
