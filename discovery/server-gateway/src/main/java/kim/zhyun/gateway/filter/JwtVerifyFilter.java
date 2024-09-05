package kim.zhyun.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class JwtVerifyFilter extends AbstractGatewayFilterFactory<JwtVerifyFilter.Config> {

    private final SecretKey KEY;
    private final String HEADER;
    private final String JWT_PREFIX;

    private final String EXCEPTION_MESSAGE_NOT_FOUND_JWT = "토큰을 찾을 수 없습니다.";
    private final String EXCEPTION_MESSAGE_MALFORMED_JWT = "잘못된 JWT 서명입니다.";
    private final String EXCEPTION_MESSAGE_EXPIRED_JWT = "만료된 JWT 토큰입니다";
    private final String EXCEPTION_MESSAGE_UNSUPPORTED_JWT = "지원되지 않는 JWT 토큰입니다";
    private final String EXCEPTION_MESSAGE_ILLEGAL_JWT = "JWT 토큰이 잘못되었습니다";

    
    public JwtVerifyFilter(@Value("${token.secret}") String secret,
                           @Value("${token.header}") String header,
                           @Value("${token.prefix}") String jwtPrefix) {
        super(JwtVerifyFilter.Config.class);
        byte[] decode = Decoders.BASE64.decode(secret);
        KEY = Keys.hmacShaKeyFor(decode);
        HEADER = header;
        JWT_PREFIX = jwtPrefix;
    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            var reqHeaders = Optional.ofNullable(request.getHeaders().get(HEADER));

            if (reqHeaders.isEmpty())
                throw new JwtException(EXCEPTION_MESSAGE_NOT_FOUND_JWT);

            String jwt = getJwtFromHeaders(reqHeaders.get());
            isJwtValid(jwt);

            return chain.filter(exchange);
        };
    }

    private String getJwtFromHeaders(List<String> reqHeaders) {
        Optional<String> reqJwtHeaderContainer = Optional.ofNullable(reqHeaders.get(0));

        if (reqJwtHeaderContainer.isEmpty())
            throw new JwtException(EXCEPTION_MESSAGE_NOT_FOUND_JWT);

        String[] reqJwtHeader = reqJwtHeaderContainer.get().trim().split(" ");
        String headerKey = reqJwtHeader[0];

        if (!headerKey.equalsIgnoreCase(JWT_PREFIX))
            throw new JwtException(EXCEPTION_MESSAGE_ILLEGAL_JWT);

        return reqJwtHeader[1];
    }

    private void isJwtValid(String jwt) {
        try {
            Jwts.parser().verifyWith(KEY).build().parseSignedClaims(jwt);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new JwtException(EXCEPTION_MESSAGE_MALFORMED_JWT);
        } catch (ExpiredJwtException e) {
            throw new JwtException(EXCEPTION_MESSAGE_EXPIRED_JWT);
        } catch (UnsupportedJwtException e) {
            throw new JwtException(EXCEPTION_MESSAGE_UNSUPPORTED_JWT);
        } catch (IllegalArgumentException e) {
            throw new JwtException(EXCEPTION_MESSAGE_ILLEGAL_JWT);
        }
    }
    
    public static class Config { }
}
