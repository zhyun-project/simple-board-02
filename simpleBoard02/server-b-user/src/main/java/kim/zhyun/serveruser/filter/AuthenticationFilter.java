package kim.zhyun.serveruser.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kim.zhyun.jwt.data.JwtUserDto;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.serveruser.data.SignInRequest;
import kim.zhyun.serveruser.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Set;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static kim.zhyun.jwt.data.JwtConstants.JWT_HEADER;
import static kim.zhyun.serveruser.data.message.ResponseMessage.SUCCESS_FORMAT_SIGN_IN;
import static kim.zhyun.serveruser.utils.FilterApiResponseUtil.sendMessage;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final MemberService userService;
    private final JwtProvider jwtProvider;
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            SignInRequest credential = new ObjectMapper().readValue(request.getInputStream(), SignInRequest.class);
            
            if (credential == null)
                return SecurityContextHolder.getContext().getAuthentication();
                
            String role = userService.findByEmail(credential.getEmail()).getRole().getGrade();
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credential.getEmail(),
                            credential.getPassword(),
                            Set.of(new SimpleGrantedAuthority(role))));
        
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        String token = jwtProvider.createToken(authResult);
        JwtUserDto principal = (JwtUserDto) authResult.getPrincipal();
        
        response.addHeader(JWT_HEADER, token);
        
        sendMessage(response,
                SC_OK,
                true,
                String.format(SUCCESS_FORMAT_SIGN_IN, principal.getNickname(), principal.getEmail()));
    }
    
}