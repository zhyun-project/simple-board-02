package kim.zhyun.serveruser.advice;

import kim.zhyun.serveruser.data.message.ExceptionMessage;
import lombok.Getter;

@Getter
public class MailAuthException extends RuntimeException {
    private final ExceptionMessage exceptionMessage;
    
    public MailAuthException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getDescription());
        this.exceptionMessage = exceptionMessage;
    }
    
}
