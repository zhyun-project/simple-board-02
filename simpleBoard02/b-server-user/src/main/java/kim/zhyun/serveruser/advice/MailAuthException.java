package kim.zhyun.serveruser.advice;

public class MailAuthException extends RuntimeException {
    
    public MailAuthException(String exceptionMessage) {
        super(exceptionMessage);
    }
    
}
