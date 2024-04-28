package kim.zhyun.serveruser.common.advice;

public class MailAuthException extends RuntimeException {
    
    public MailAuthException(String exceptionMessage) {
        super(exceptionMessage);
    }
    
}
