package kim.zhyun.serveruser.advice;

import kim.zhyun.serveruser.data.type.ExceptionType;
import lombok.Getter;

@Getter
public class MailAuthException extends RuntimeException {
    private final ExceptionType exceptionType;
    
    public MailAuthException(ExceptionType exceptionType) {
        super(exceptionType.getDescription());
        this.exceptionType = exceptionType;
    }
    
}
