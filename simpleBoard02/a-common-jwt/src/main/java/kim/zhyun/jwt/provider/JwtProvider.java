package kim.zhyun.jwt.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kim.zhyun.jwt.common.constants.JwtConstants;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.jwt.domain.repository.JwtUserInfoRepository;
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

import static kim.zhyun.jwt.common.constants.JwtConstants.JWT_CLAIM_KEY_USER_ID;
import static kim.zhyun.jwt.common.constants.JwtExceptionMessageConstants.JWT_EXPIRED;
import static kim.zhyun.jwt.common.constants.JwtExceptionMessageConstants.JWT_EXPIRED_IS_NULL;
import static kim.zhyun.jwt.util.TimeUnitUtil.timeUnitFrom;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider implements InitializingBean {
    
    private final JwtUserInfoRepository jwtUserInfoRepository;
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
        
        Optional<JwtUserInfoEntity> userInfoContainer = jwtUserInfoRepository.findById(id);
        
        if (userInfoContainer.isEmpty())
            throw new JwtException(JWT_EXPIRED);
        
        JwtUserInfoEntity userInfo = userInfoContainer.get();
        String nickname = userInfo.getNickname();
        String grade = userInfo.getGrade();
        
        return new UsernamePasswordAuthenticationToken(
                JwtUserInfoDto.builder()
                        .id(id)
                        .email(email)
                        .nickname(nickname).build(),
                token,
                Arrays.stream(grade.split(JwtConstants.JWT_CLAIM_GRADE_SEPARATOR))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet()));
    }
    
    /**
     * token -> email 추출
     */
    public String emailFrom(String token) {
        return claimsFrom(token.trim()).getSubject();
    }
    
    /**
     * token -> id 추출
     */
    public Long idFrom(String token) {
        return claimsFrom(token.trim()).get(JWT_CLAIM_KEY_USER_ID, Long.class);
    }
    
    
    /**
     * token -> claim 추출
     */
    private Claims claimsFrom(String token) {
        return Jwts.parser()
                .verifyWith(key).build()
                .parseSignedClaims(token.trim())
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
        return ((JwtUserInfoDto) authentication.getPrincipal()).getEmail();
    }
    
    /**
     * authentication -> nickname 추출
     */
    public String nicknameFrom(Authentication authentication) {
        return ((JwtUserInfoDto) authentication.getPrincipal()).getNickname();
    }
    
    /**
     * authentication -> id 추출
     */
    private Long idFrom(Authentication authentication) {
        return ((JwtUserInfoDto) authentication.getPrincipal()).getId();
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