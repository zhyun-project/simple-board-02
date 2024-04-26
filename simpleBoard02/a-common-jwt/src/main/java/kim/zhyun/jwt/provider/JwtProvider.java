package kim.zhyun.jwt.provider;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kim.zhyun.jwt.data.JwtConstants;
import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.jwt.data.JwtUserInfo;
import kim.zhyun.jwt.repository.JwtUserInfoRepository;
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static kim.zhyun.jwt.data.JwtConstants.JWT_CLAIM_KEY_USER_ID;
import static kim.zhyun.jwt.data.JwtResponseMessage.*;
import static kim.zhyun.jwt.util.TimeUnitUtil.timeUnitFrom;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider implements InitializingBean {
    
    private final JwtUserInfoRepository userInfoStorage;
    private final JwtConstants jwtItems;
    private SecretKey key;
    
    public Long expiredTime;
    public TimeUnit expiredTimeUnit;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(jwtItems.secretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
    }
    
    public void setJwtExpired(Long expiredTime,
                              String expiredTimeUnitString) {
        this.expiredTime = expiredTime;
        this.expiredTimeUnit = TimeUnit.of(timeUnitFrom(expiredTimeUnitString));
    }
    
    /**
     * security context -> jwt
     */
    public String tokenFrom(Authentication authentication) {
        
        if (expiredTime == null || expiredTimeUnit == null)
            throw new JwtException(JWT_EXPIRED_IS_NULL);
        
        return Jwts.builder()
                .subject(emailFrom(authentication))
                .claim(JWT_CLAIM_KEY_USER_ID, idFrom(authentication))
                .expiration(expiredDate())
                .signWith(key)
                .compact();
    }
    
    /**
     * jwt -> security context
     */
    public Authentication authenticationFrom(String token) {
        Claims claims = claimsFrom(token);
        
        Long id = claims.get(JWT_CLAIM_KEY_USER_ID, Long.class);
        String email = claims.getSubject();
        
        Optional<JwtUserInfo> userInfoContainer = userInfoStorage.findById(id);
        
        if (userInfoContainer.isEmpty())
            throw new JwtException(JWT_EXPIRED);
        
        JwtUserInfo userInfo = userInfoContainer.get();
        String nickname = userInfo.getNickname();
        String grade = userInfo.getGrade();
        
        return new UsernamePasswordAuthenticationToken(
                JwtUserDto.builder()
                        .id(id)
                        .email(email)
                        .nickname(nickname).build(),
                token,
                Arrays.stream(grade.split(JwtConstants.JWT_CLAIM_GRADE_SEPARATOR))
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
    
    
    /**
     * token -> email 추출
     */
    public String emailFrom(String token) {
        return claimsFrom(token).getSubject();
    }
    
    /**
     * token -> id 추출
     */
    public Long idFrom(String token) {
        return claimsFrom(token).get(JWT_CLAIM_KEY_USER_ID, Long.class);
    }
    
    
    /**
     * token -> claim 추출
     */
    private Claims claimsFrom(String token) {
        return Jwts.parser()
                .verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * 만료일자 계산
     */
    private Date expiredDate() {
        return Date.from(new Date(System.currentTimeMillis())
                        .toInstant()
                        .plus(expiredTime, expiredTimeUnit.toChronoUnit()));
    }
    
    /**
     * authentication -> email 추출
     */
    public String emailFrom(Authentication authentication) {
        return ((JwtUserDto) authentication.getPrincipal()).getEmail();
    }
    
    /**
     * authentication -> nickname 추출
     */
    public String nicknameFrom(Authentication authentication) {
        return ((JwtUserDto) authentication.getPrincipal()).getNickname();
    }
    
    /**
     * authentication -> id 추출
     */
    private Long idFrom(Authentication authentication) {
        return ((JwtUserDto) authentication.getPrincipal()).getId();
    }
    
    /**
     * authentication -> 권한(grade) 추출
     */
    public String gradeFrom(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(JwtConstants.JWT_CLAIM_GRADE_SEPARATOR));
    }
}