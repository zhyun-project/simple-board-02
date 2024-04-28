package kim.zhyun.serveruser.common.advice;

public class ApiException extends RuntimeException {
    
    public ApiException(String exceptionMessage) {
        super(exceptionMessage);
    }
    
}
