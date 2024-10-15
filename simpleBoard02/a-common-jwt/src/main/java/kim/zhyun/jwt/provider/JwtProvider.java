package kim.zhyun.jwt.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kim.zhyun.jwt.common.constants.JwtConstants;
import kim.zhyun.jwt.domain.converter.JwtUserInfoConverter;
import kim.zhyun.jwt.domain.dto.JwtAuthentication;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.domain.repository.JwtUserInfoEntity;
import kim.zhyun.jwt.domain.repository.JwtUserInfoRepository;
import kim.zhyun.jwt.exception.ApiException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static kim.zhyun.jwt.common.constants.JwtConstants.JWT_CLAIM_KEY_USER_ID;
import static kim.zhyun.jwt.exception.message.CommonExceptionMessage.EXCEPTION_NOT_FOUND;
import static kim.zhyun.jwt.util.TimeUnitUtil.timeUnitFrom;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider implements InitializingBean {
    
    private final JwtUserInfoRepository jwtUserInfoRepository;
    private final JwtConstants jwtItems;
    private final JwtUserInfoConverter jwtUserInfoConverter;
    private SecretKey key;

    @Getter
    private int expiredTime = 30;

    private String expiredTimeUnit = "d";
    // Getter
    public TimeUnit getExpiredTimeUnit() {
        return TimeUnit.of(timeUnitFrom(expiredTimeUnit));
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(jwtItems.secretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * jwt 시간 변경 시 docker compose 설정을 통하기 위한 설정
     */
    public void setJwtExpired(int expiredTime,
                              String expiredTimeUnitString) {
        this.expiredTime = expiredTime;
        this.expiredTimeUnit = expiredTimeUnitString;
    }
    
    /**
     * JwtUserInfoDto -> jwt
     */
    public String tokenFrom(JwtUserInfoDto userInfoDto) {
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
    public JwtAuthentication authenticationFrom(String token) {
        token = token.trim();
        Claims claims = claimsFrom(token);
        
        Long id = claims.get(JWT_CLAIM_KEY_USER_ID, Long.class);

        Optional<JwtUserInfoEntity> userInfoContainer = jwtUserInfoRepository.findById(id);
        
        if (userInfoContainer.isEmpty())
            throw new ApiException(EXCEPTION_NOT_FOUND);
        
        JwtUserInfoEntity userInfo = userInfoContainer.get();
        JwtUserInfoDto userInfoDto = jwtUserInfoConverter.toDto(userInfo);

        return new JwtAuthentication(
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
                        .plus(expiredTime, getExpiredTimeUnit().toChronoUnit()));
    }

}