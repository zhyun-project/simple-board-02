package kim.zhyun.serveruser.advice;

import kim.zhyun.serveruser.data.message.ExceptionMessage;
import lombok.Getter;

@Getter
public class NotFoundSessionException extends RuntimeException {
    private final ExceptionMessage exceptionMessage;
    
    public NotFoundSessionException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getDescription());
        this.exceptionMessage = exceptionMessage;
    }
    
}
