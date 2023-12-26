package kim.zhyun.serveruser.data.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionType {
    NOT_FOUND_SESSION("사용자를 찾을 수 없습니다.")
    ;
    
    private final String description;
}
