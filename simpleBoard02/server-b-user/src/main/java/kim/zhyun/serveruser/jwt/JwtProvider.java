package kim.zhyun.serveruser.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kim.zhyun.serveruser.data.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {
    
    @Value("${token.secret}")
    private String SECRET;
    @Value("${token.expiration-time}")
    private long EXPIRATION;
    
    public String createToken(UserDto user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("nickname", user.getNickname())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(secretKey())
                .compact();
    }
    
    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }
}