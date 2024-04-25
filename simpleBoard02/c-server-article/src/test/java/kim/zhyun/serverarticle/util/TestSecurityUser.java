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
    public static final String ADMIN_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MiwiZXhwIjoxNzE2NTkyNTk4fQ.h1d-ZBc9NzR0PGRDHSnbs-PsLahg1p2LfkGJiaxwxI3YzZJ6mATrjHFKy-7U8IONraAvYsNPX6c6knNztZCzxA";
    public static final String MEMBER1_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BkYXVtLm5ldCIsImlkIjozLCJleHAiOjE3MTY1OTI3MTR9.XWGlv8oqCrZS7EFrLlNb4osq4Mqo-DgE8djdljPa1JZgP9M8-y8ZevH_q7wS3TY6UhCACRq5z_Q9hX0HPJfziQ";
    public static final String MEMBER2_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BrYWthby5jb20iLCJpZCI6MSwiZXhwIjoxNzE2NTkyNjMwfQ.DG8ph3HoxkHp7xyKDMQxZkvFRPkZHNyFTr26ASz02VYeGG-4OFwaPgRLMhI63yvDyVBKWYf4TL-q7ihVj7v1FA";
    
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
    
    public static JwtUserDto getJwtUserDto(JwtProvider jwtProvider, String userType) {
        setAuthentication(jwtProvider, userType);
        return JwtUserDto.from(TestSecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
