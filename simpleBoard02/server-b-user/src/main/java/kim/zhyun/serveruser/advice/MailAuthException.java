package kim.zhyun.serveruser.advice;

import lombok.Getter;

@Getter
public class MailAuthException extends RuntimeException {
    private final String exceptionMessage;
    
    public MailAuthException(String exceptionMessage) {
        super(exceptionMessage);
        this.exceptionMessage = exceptionMessage;
    }
    
}
