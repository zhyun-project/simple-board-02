package kim.zhyun.jwt.data;

public class JwtResponseMessage {
    public static final String JWT_INVALID_SIGNATURE = "잘못된 JWT 서명입니다.";
    public static final String JWT_EXPIRED = "만료된 JWT 토큰입니다.";
    public static final String JWT_UNSUPPORTED = "지원되지 않는 JWT 토큰입니다.";
    public static final String JWT_DECODE_FAIL = "JWT 토큰이 잘못되었습니다.";
    
    public static final String JWT_EXPIRED_IS_NULL = "JWT 만료 시간 설정이 필요합니다.";
}
