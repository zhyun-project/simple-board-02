package kim.zhyun.serveruser.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kim.zhyun.jwt.domain.converter.JwtUserInfoConverter;
import kim.zhyun.jwt.domain.dto.JwtUserInfoDto;
import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.serveruser.config.security.SecurityAuthenticationManager;
import kim.zhyun.serveruser.filter.model.SignInRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static kim.zhyun.jwt.common.constants.JwtConstants.JWT_HEADER;
import static kim.zhyun.jwt.exception.message.CommonExceptionMessage.EXCEPTION_REQUIRED_REQUEST_BODY;
import static kim.zhyun.jwt.util.FilterApiResponseUtil.sendMessage;
import static kim.zhyun.serveruser.common.message.ResponseMessage.RESPONSE_SUCCESS_FORMAT_SIGN_IN;

@Slf4j
@Component
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtProvider jwtProvider;
    
    public AuthenticationFilter(SecurityAuthenticationManager securityAuthenticationManager,
                                JwtProvider jwtProvider,
                                @Value("${token.expiration-time-unit}") String expiredTimeUnit,
                                @Value("${token.expiration-time}") Long expiredTime) {
        super(securityAuthenticationManager);
        
        this.jwtProvider = jwtProvider;
        this.jwtProvider.setJwtExpired(expiredTime, expiredTimeUnit);
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            SignInRequest credential = new ObjectMapper().readValue(request.getInputStream(), SignInRequest.class);
            
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credential.getEmail(),
                            credential.getPassword(),
                            null));
        
        } catch (IOException e) {
            throw new ApiException(EXCEPTION_REQUIRED_REQUEST_BODY);
        }
    }
    
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");

        String token = jwtProvider.tokenFrom(authResult);
        response.addHeader(JWT_HEADER, token);

        JwtUserInfoDto userInfoDto = JwtUserInfoConverter.toDto(authResult.getPrincipal());

        sendMessage(response,
                SC_OK,
                true,
                RESPONSE_SUCCESS_FORMAT_SIGN_IN.formatted(jwtProvider.nicknameFrom(authResult), jwtProvider.emailFrom(authResult)),
                userInfoDto);
    }
    
}