package kim.zhyun.serveruser.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kim.zhyun.serveruser.data.UserDto;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider implements InitializingBean {
    
    @Value("${token.secret}")
    private String SECRET;
    @Value("${token.expiration-time}")
    private long EXPIRATION;
    
    private SecretKey key;
    
    public String createToken(UserDto user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("nickname", user.getNickname())
                .claim("grade", user.getRole().getGrade())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        key = Keys.hmacShaKeyFor(keyBytes); // InitializingBean 을 구현한 이유. base64로 인코딩 된 secret키를 디코딩 후 key에 할당하기 위함
    }
    
}