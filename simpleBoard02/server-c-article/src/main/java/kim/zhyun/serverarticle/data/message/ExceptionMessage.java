package kim.zhyun.serverarticle.data.message;

import lombok.Getter;

@Getter
public class ExceptionMessage {
    
    // 공통
    public static final String EXCEPTION_VALID_FORMAT = "입력 값이 올바르지 않습니다.";
    public static final String EXCEPTION_REQUIRED_REQUEST_BODY = "Required request body is missing";
    public static final String EXCEPTION_PERMISSION = "권한이 없습니다.";
    public static final String EXCEPTION_AUTHENTICATION = "로그인이 필요합니다.";
    public static final String EXCEPTION_NOT_FOUND = "잘못된 요청입니다.";
    
}
