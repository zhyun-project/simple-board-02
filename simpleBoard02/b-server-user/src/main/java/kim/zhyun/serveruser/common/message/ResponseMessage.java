package kim.zhyun.serveruser.common.message;

import lombok.Getter;

@Getter
public class ResponseMessage {
    // 회원 가입
    public static final String RESPONSE_SUCCESS_FORMAT_SIGN_UP = "%s님 가입을 축하합니다! 🥳";
    
    // email 관련
    public static final String RESPONSE_SIGN_UP_AVAILABLE_EMAIL = "사용 가능한 이메일입니다. 이메일 인증을 진행해주세요.";
    public static final String RESPONSE_SIGN_UP_UNAVAILABLE_EMAIL = "이미 사용중인 이메일입니다.";
    public static final String RESPONSE_SEND_EMAIL_AUTH_CODE = "인증 코드가 전송되었습니다.";
    public static final String RESPONSE_VERIFY_EMAIL_AUTH_SUCCESS = "인증되었습니다.";
    
    // nickname 관련
    public static final String RESPONSE_SIGN_UP_AVAILABLE_NICKNAME = "사용 가능한 닉네임입니다.";
    public static final String RESPONSE_SIGN_UP_UNAVAILABLE_NICKNAME = "이미 사용중인 닉네임입니다.";
    
    // sign-up 값 체크 기본 응답
    public static final String RESPONSE_SIGN_UP_CHECK_VALUE_IS_EMPTY = "값을 입력해주세요.";
    
    // 로그인 _ ex. 얼거스(gimwlgus@daum.net)님 로그인 되었습니다
    public static final String RESPONSE_SUCCESS_FORMAT_SIGN_IN = "%s(%s)님 로그인 되었습니다.";
    public static final String RESPONSE_SUCCESS_FORMAT_SIGN_OUT = "%s(%s)님 로그아웃 되었습니다.";
    
    // 계정 관련
    public static final String RESPONSE_USER_REFERENCE_ALL = "전체 계정 상세 조회";
    public static final String RESPONSE_USER_REFERENCE_ME = "계정 상세 조회";
    public static final String RESPONSE_USER_INFO_UPDATE = "%s님 계정 정보가 수정되었습니다."; // %s : 닉네임
    public static final String RESPONSE_USER_GRADE_UPDATE = "%s님 권한이 %s(으)로 수정되었습니다."; // %s : 닉네임, 권한
    public static final String RESPONSE_USER_WITHDRAWAL = "%s(%s)님 탈퇴되었습니다."; // %s : 닉네임, 이메일
    
}
