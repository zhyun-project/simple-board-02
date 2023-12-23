package kim.zhyun.serveruser.data.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleType {
    ADMIN("관리자"),
    MEMBER("회원"),
    WITHDRAWAL("탈퇴 회원");
    
    private final String description;
}
