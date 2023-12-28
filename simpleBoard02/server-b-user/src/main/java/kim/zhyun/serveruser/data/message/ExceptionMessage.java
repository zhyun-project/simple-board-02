package kim.zhyun.serveruser.data.message;

import lombok.Getter;

@Getter
public class ExceptionMessage {
    
    // email 관련
    public static final String MAIL_SEND_FAIL = "메일 발송에 실패했습니다. 다른 이메일 주소를 사용해주세요.";
    public static final String REQUIRE_MAIL_DUPLICATE_CHECK = "이메일 중복 확인을 먼저 진행해 주세요.";
    public static final String VERIFY_EMAIL_AUTH_CODE_EXPIRED = "인증 번호가 만료되었습니다. 인증을 다시 진행해주세요!";
    public static final String VERIFY_FAIL_EMAIL_AUTH_CODE = "인증 번호가 일치하지 않습니다.";
    public static final String VALID_EMAIL_EXCEPTION_MESSAGE = "올바른 이메일 주소를 입력해주세요.";
    public static final String VALID_EMAIL_CODE_EXCEPTION_MESSAGE = "인증 코드를 입력해 주세요.";
    
    // nickname 관련
    public static final String VALID_NICKNAME_EXCEPTION_MESSAGE = "1글자 이상, 6글자 이하로 입력해주세요.";
    
    // 공통
    public static final String VALID_EXCEPTION = "입력 값이 올바르지 않습니다.";
    public static final String REQUIRED_REQUEST_BODY = "Required request body is missing";
    
}
