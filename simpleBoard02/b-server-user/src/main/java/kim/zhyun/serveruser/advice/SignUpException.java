package kim.zhyun.serveruser.advice;

public class SignUpException extends RuntimeException {
    
    public SignUpException(String exceptionMessage) {
        super(exceptionMessage);
    }
    
}
