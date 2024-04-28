package kim.zhyun.serveruser.common.message;

public class ExceptionMessage {
    
    // email 관련
    public static final String EXCEPTION_MAIL_SEND_FAIL = "메일 발송에 실패했습니다. 다른 이메일 주소를 사용해주세요.";
    public static final String EXCEPTION_REQUIRE_MAIL_DUPLICATE_CHECK = "이메일 중복 확인을 먼저 진행해 주세요.";
    public static final String EXCEPTION_VERIFY_EMAIL_AUTH_CODE_EXPIRED = "인증 번호가 만료되었습니다. 인증을 다시 진행해주세요!";
    public static final String EXCEPTION_VERIFY_FAIL_EMAIL_AUTH_CODE = "인증 번호가 일치하지 않습니다.";
    public static final String EXCEPTION_VALID_EMAIL_FORMAT = "올바른 이메일 주소를 입력해주세요.";
    public static final String EXCEPTION_VALID_EMAIL_CODE = "인증 코드를 입력해 주세요.";
    
    // nickname 관련
    public static final String EXCEPTION_VALID_NICKNAME_FORMAT = "1글자 이상, 6글자 이하로 입력해주세요.";
    public static final String EXCEPTION_REQUIRE_NICKNAME_DUPLICATE_CHECK = "닉네임 중복 확인을 먼저 진행해 주세요.";
    
    // password 관련
    public static final String EXCEPTION_VALID_PASSWORD_FORMAT = "4글자 이상 입력해주세요.";

    // 로그인 관련
    public static final String EXCEPTION_SIGNIN_FAIL = "일치하는 사용자 정보가 없습니다.";
    public static final String EXCEPTION_WITHDRAWAL = "탈퇴한지 %02d일 %02d시 %02d분 경과한 사용자입니다.";
    
    
    // 공통
    public static final String EXCEPTION_VALID_FORMAT = "입력 값이 올바르지 않습니다.";
    public static final String EXCEPTION_REQUIRED_REQUEST_BODY = "Required request body is missing";
    public static final String EXCEPTION_PERMISSION = "권한이 없습니다.";
    public static final String EXCEPTION_AUTHENTICATION = "로그인이 필요합니다.";
    public static final String EXCEPTION_NOT_FOUND = "잘못된 요청입니다.";
    
    // 회원 권한
    public static final String EXCEPTION_ALREADY_WITHDRAWN_MEMBER = "탈퇴한 회원입니다.";
    
}
