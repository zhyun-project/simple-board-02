package kim.zhyun.serveruser.advice;

import kim.zhyun.serveruser.data.type.ExceptionType;
import lombok.Getter;

@Getter
public class NotFoundSessionException extends RuntimeException {
    private final ExceptionType exceptionType;
    
    public NotFoundSessionException(ExceptionType exceptionType) {
        super(exceptionType.getDescription());
        this.exceptionType = exceptionType;
    }
    
}
