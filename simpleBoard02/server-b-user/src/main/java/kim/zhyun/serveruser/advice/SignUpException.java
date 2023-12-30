package kim.zhyun.serveruser.advice;

import lombok.Getter;

@Getter
public class SignUpException extends RuntimeException {
    
    public SignUpException(String exceptionMessage) {
        super(exceptionMessage);
    }
    
}
