package kim.zhyun.serveruser.data.message;

import lombok.Getter;

@Getter
public class ResponseMessage {
    // email 관련
    public static final String SIGN_UP_AVAILABLE_EMAIL = "사용 가능한 이메일입니다. 이메일 인증을 진행해주세요.";
    public static final String SIGN_UP_UNAVAILABLE_EMAIL = "이미 사용중인 이메일입니다.";
    public static final String SEND_EMAIL_AUTH_CODE = "인증 코드가 전송되었습니다.";
    public static final String VERIFY_EMAIL_AUTH_SUCCESS = "인증되었습니다.";
    
    // nickname 관련
    public static final String SIGN_UP_AVAILABLE_NICKNAME = "사용 가능한 닉네임입니다.";
    public static final String SIGN_UP_UNAVAILABLE_NICKNAME = "이미 사용중인 닉네임입니다.";
    
    // sign-up 값 체크 기본 응답
    public static final String SIGN_UP_CHECK_VALUE_IS_EMPTY = "값을 입력해주세요.";
    
}
