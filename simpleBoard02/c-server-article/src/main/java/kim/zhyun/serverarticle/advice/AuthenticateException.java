package kim.zhyun.serverarticle.advice;

public class AuthenticateException extends RuntimeException {
    
    public AuthenticateException(String message) {
        super(message);
    }
    
}
