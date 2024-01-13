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

@Slf4j
@Component
public class JwtVerifyFilter extends AbstractGatewayFilterFactory<JwtVerifyFilter.Config> {
    private final SecretKey KEY;
    private final String HEADER = "X-TOKEN";
    
    public JwtVerifyFilter(@Value("${token.secret}") String secret) {
        super(JwtVerifyFilter.Config.class);
        byte[] decode = Decoders.BASE64.decode(secret);
        KEY = Keys.hmacShaKeyFor(decode);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            if (!request.getHeaders().containsKey(HEADER))
                throw new JwtException("토큰을 찾을 수 없습니다.");
            
            String authorizationHeader = request.getHeaders().get(HEADER).get(0);
            String jwt = authorizationHeader.replace("Bearer ", "");
            
            isJwtValid(jwt);
            
            return chain.filter(exchange);
        };
    }
    
    private void isJwtValid(String jwt) {
        try {
            Jwts.parser().verifyWith(KEY).build().parseSignedClaims(jwt);
            
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new JwtException("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            throw new JwtException("만료된 JWT 토큰입니다");
        } catch (UnsupportedJwtException e) {
            throw new JwtException("지원되지 않는 JWT 토큰입니다");
        } catch (IllegalArgumentException e) {
            throw new JwtException("JWT 토큰이 잘못되었습니다");
        }
    }
    
    
    public static class Config { }
}
