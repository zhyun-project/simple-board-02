package kim.zhyun.serveruser.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import kim.zhyun.serveruser.common.annotation.constraint.NicknameValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_VALID_EMAIL_CODE;
import static kim.zhyun.serveruser.common.message.ExceptionMessage.EXCEPTION_VALID_NICKNAME_FORMAT;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NicknameValidator.class)
public @interface Nickname {
    
    String message() default EXCEPTION_VALID_NICKNAME_FORMAT;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
}
