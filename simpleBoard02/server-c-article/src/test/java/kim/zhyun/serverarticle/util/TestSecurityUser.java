package kim.zhyun.serverarticle.util;

import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.jwt.provider.JwtProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.TestSecurityContextHolder;

@Disabled("테스트 유틸")
public class TestSecurityUser {
    public static final String ADMIN_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MiwiZXhwIjoxNzA3NDE3NTc2fQ.3p1pk4Il-lmtq8jlYS5xyKd_78ehPYt-WyVHkN6XrvYq6fGCfnLdRmZrPOvC52nZBcYfGLzz7wUxR8dXOzlQug";
    public static final String MEMBER1_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BkYXVtLm5ldCIsImlkIjozLCJleHAiOjE3MDc0MTc1OTN9.HcSGR1n6CHsXeztwnSKUFZq01L4quLRIyeJVskpmSKQsKtmWChcX9cZgT-XIN8egFOa71kpjiE62sFq8zlWILw";
    public static final String MEMBER2_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BrYWthby5jb20iLCJpZCI6NSwiZXhwIjoxNzA3NDE3NTU4fQ.ET6WN0c6uilPXubDWy7PDbMRP_aZdSXRoRn-BgC9HhQ6aFO_vGnAlzeKl8b3DoPDai0Qg5lKz3iuBtEtUKwWDg";
    
    @DisplayName("Authentication 객체 설정")
    public static void setAuthentication(JwtProvider provider, String userType) {
        String token = userType.equalsIgnoreCase("admin") ? ADMIN_TOKEN
                : userType.equalsIgnoreCase("member1") ? MEMBER1_TOKEN
                : MEMBER2_TOKEN;
        
        Authentication authentication = provider.authenticationFrom(token);
        
        SecurityContext securityContext = TestSecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        TestSecurityContextHolder.setContext(securityContext);
    }
    
    public static JwtUserDto getJwtUserDto() {
        return JwtUserDto.from(TestSecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
