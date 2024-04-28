package kim.zhyun.serveruser.common.advice;

public class SignUpException extends RuntimeException {
    
    public SignUpException(String exceptionMessage) {
        super(exceptionMessage);
    }
    
}
