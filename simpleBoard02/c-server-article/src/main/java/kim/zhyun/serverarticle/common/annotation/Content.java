package kim.zhyun.serverarticle.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static kim.zhyun.serverarticle.common.message.ExceptionMessage.EXCEPTION_CONTENT_IS_NULL;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContentValidator.class)
public @interface Content {
    
    String message() default EXCEPTION_CONTENT_IS_NULL;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
}
