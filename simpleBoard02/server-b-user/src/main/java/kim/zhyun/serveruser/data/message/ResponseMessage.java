package kim.zhyun.serveruser.data.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {
    SIGN_UP_AVAILABLE_EMAIL("사용 가능한 이메일입니다. 이메일 인증을 진행해주세요."),
    SIGN_UP_UNAVAILABLE_EMAIL("이미 사용중인 이메일입니다."),
    
    SIGN_UP_AVAILABLE_NICKNAME("사용 가능한 닉네임입니다."),
    SIGN_UP_UNAVAILABLE_NICKNAME("이미 사용중인 닉네임입니다."),

    SIGN_UP_CHECK_VALUE_IS_EMPTY("값을 입력해주세요."),
    VALID_EXCEPTION("입력 값이 올바르지 않습니다."),
    
    VERIFY_EMAIL_AUTH_SUCCESS("인증되었습니다."),
    ;
    
    private final String message;
}
