package kim.zhyun.serveruser.data.message;

import lombok.Getter;

@Getter
public class ResponseMessage {
    // 회원 가입
    public static final String SUCCESS_FORMAT_SIGN_UP = "%s님 가입을 축하합니다! 🥳";
    
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
    
    // 로그인 _ ex. 얼거스(gimwlgus@daum.net)님 로그인 되었습니다
    public static final String SUCCESS_FORMAT_SIGN_IN = "%s(%s)님 로그인 되었습니다.";
    public static final String SUCCESS_FORMAT_SIGN_OUT = "%s(%s)님 로그아웃 되었습니다.";
    
    // 계정 관련
    public static final String USER_REFERENCE_ALL = "전체 계정 상세 조회";
    public static final String USER_REFERENCE_ME = "계정 상세 조회";
    
}
