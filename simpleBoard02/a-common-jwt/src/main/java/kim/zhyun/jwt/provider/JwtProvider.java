package kim.zhyun.jwt.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kim.zhyun.jwt.common.constants.JwtConstants;
import kim.zhyun.jwt.domain.converter.JwtUserInfoConverter;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.jwt.domain.repository.JwtUserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final JwtUserInfoConverter jwtUserInfoConverter;
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

        JwtUserInfoDto userInfoDto = JwtUserInfoConverter.toDto(authentication);
        return Jwts.builder()
                .subject(userInfoDto.getEmail())
                .claim(JWT_CLAIM_KEY_USER_ID, userInfoDto.getId())
                .expiration(expiredDate())
                .signWith(key)
                .compact();
    }
    
    /**
     * jwt -> security context
     */
    public Authentication authenticationFrom(String token) {
        token = token.trim();
        Claims claims = claimsFrom(token);
        
        Long id = claims.get(JWT_CLAIM_KEY_USER_ID, Long.class);

        Optional<JwtUserInfoEntity> userInfoContainer = jwtUserInfoRepository.findById(id);
        
        if (userInfoContainer.isEmpty())
            throw new JwtException(JWT_EXPIRED);
        
        JwtUserInfoEntity userInfo = userInfoContainer.get();
        JwtUserInfoDto userInfoDto = jwtUserInfoConverter.toDto(userInfo);

        return new UsernamePasswordAuthenticationToken(
                userInfoDto,
                token,
                Arrays.stream(userInfo.getGrade().split(JwtConstants.JWT_CLAIM_GRADE_SEPARATOR))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet()));
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

}