package kim.zhyun.jwt.exception;

public class ApiException extends RuntimeException {
    
    public ApiException(String exceptionMessage) {
        super(exceptionMessage);
    }
    
}
