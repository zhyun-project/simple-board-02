package kim.zhyun.serveruser.advice;

import lombok.Getter;

@Getter
public class NotFoundSessionException extends RuntimeException {
    private final String exceptionMessage;
    
    public NotFoundSessionException(String exceptionMessage) {
        super(exceptionMessage);
        this.exceptionMessage = exceptionMessage;
    }
    
}
