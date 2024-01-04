package kim.zhyun.jwt.provider;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import static kim.zhyun.jwt.data.JwtConstants.JWT_CLAIM_GRADE_SEPARATOR;
import static kim.zhyun.jwt.data.JwtConstants.JWT_CLAIM_KEY_GRADE;
import static kim.zhyun.jwt.data.JwtResponseMessage.*;

@Slf4j
@Component
public class JwtProvider implements InitializingBean {
    
    @Value("${token.secret}")
    private String SECRET;
    @Value("${token.expiration-time}")
    private long EXPIRATION;
    
    private SecretKey key;
    
    /**
     * security context -> jwt
     */
    public String createToken(Authentication authentication) {
        return Jwts.builder()
                .subject(authentication.getName())
                .claim(JWT_CLAIM_KEY_GRADE, authentication
                        .getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(JWT_CLAIM_GRADE_SEPARATOR)))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }
    
    /**
     * jwt -> security context
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
        
        var authorities = Arrays.stream(claims.get(JWT_CLAIM_KEY_GRADE).toString().split(JWT_CLAIM_GRADE_SEPARATOR))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        
        User principal = new User(claims.getSubject(), "", authorities);
        
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }
    
    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info(JWT_INVALID_SIGNATURE);
        } catch (ExpiredJwtException e) {
            log.info(JWT_EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.info(JWT_UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            log.info(JWT_DECODE_FAIL);
        }
        return false;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        key = Keys.hmacShaKeyFor(keyBytes);
    }
    
}