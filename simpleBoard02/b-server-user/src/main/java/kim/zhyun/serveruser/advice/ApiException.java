package kim.zhyun.serveruser.advice;

public class ApiException extends RuntimeException {
    
    public ApiException(String exceptionMessage) {
        super(exceptionMessage);
    }
    
}
