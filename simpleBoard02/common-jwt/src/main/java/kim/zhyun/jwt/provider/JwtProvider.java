package kim.zhyun.jwt.provider;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kim.zhyun.jwt.data.JwtConstants;
import kim.zhyun.jwt.data.JwtUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import static kim.zhyun.jwt.data.JwtConstants.*;
import static kim.zhyun.jwt.data.JwtResponseMessage.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider implements InitializingBean {
    
    private final JwtConstants jwtItems;
    private SecretKey key;
    
    /**
     * security context -> jwt
     */
    public String createToken(Authentication authentication) {
        return Jwts.builder()
                .subject(((JwtUserDto) authentication.getPrincipal()).getEmail())
                .claim(JWT_CLAIM_KEY_USER_GRADE,authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(JwtConstants.JWT_CLAIM_GRADE_SEPARATOR)))
                .claim(JWT_CLAIM_KEY_USER_ID,((JwtUserDto)authentication.getPrincipal()).getId())
                .claim(JWT_CLAIM_KEY_USER_NICKNAME,((JwtUserDto)authentication.getPrincipal()).getNickname())
                .expiration(new Date(System.currentTimeMillis() + jwtItems.expiredTime))
                .signWith(key)
                .compact();
    }
    
    /**
     * jwt -> security context
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        
        Long id = claims.get(JWT_CLAIM_KEY_USER_ID, Long.class);
        String email = claims.getSubject();
        String nickname = claims.get(JWT_CLAIM_KEY_USER_NICKNAME, String.class);
        
        return new UsernamePasswordAuthenticationToken(
                JwtUserDto.builder()
                        .id(id)
                        .email(email)
                        .nickname(nickname).build(),
                token,
                Arrays.stream(claims.get(JWT_CLAIM_KEY_USER_GRADE).toString().split(JwtConstants.JWT_CLAIM_GRADE_SEPARATOR))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet()));
    }
    
    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new JwtException(JWT_INVALID_SIGNATURE);
        } catch (ExpiredJwtException e) {
            throw new JwtException(JWT_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new JwtException(JWT_UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            throw new JwtException(JWT_DECODE_FAIL);
        }
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(jwtItems.secretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
    }
    
    
    /**
     * token에서 email 추출
     */
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }
    
    /**
     * token에서 claim 추출
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
    }
}