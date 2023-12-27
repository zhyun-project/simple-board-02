package kim.zhyun.serveruser.data.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionType {
    NOT_FOUND_SESSION("사용자를 찾을 수 없습니다."),
    MAIL_SEND_FAIL("메일 발송에 실패했습니다. 다른 이메일 주소를 사용해주세요."),
    REQUIRE_MAIL_DUPLICATE_CHECK("이메일 중복 확인을 먼저 진행해 주세요."),
    
    RESPONSE_API_MESSAGE_INPUT_FAULT("api response \"message\"가 잘못 입력되었습니다😮");
    ;
    
    private final String description;
}
