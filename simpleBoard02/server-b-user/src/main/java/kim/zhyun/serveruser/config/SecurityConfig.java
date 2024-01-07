package kim.zhyun.serveruser.config;

import kim.zhyun.jwt.filter.JwtFilter;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.serveruser.advice.MemberException;
import kim.zhyun.serveruser.filter.AuthenticationFilter;
import kim.zhyun.serveruser.filter.ExceptionHandlerFilter;
import kim.zhyun.serveruser.filter.SessionCheckFilter;
import kim.zhyun.serveruser.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_AUTHENTICATION;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_PERMISSION;
import static org.springframework.security.config.Customizer.withDefaults;

@RequiredArgsConstructor
@EnableWebSecurity(debug = true)
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final SecurityAuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final MemberService memberService;
    
    private final SessionCheckFilter sessionCheckFilter;
    private final ExceptionHandlerFilter exceptionHandlerFilter;
    private final JwtFilter jwtFilter;

    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcMatcher = new MvcRequestMatcher.Builder(introspector);

        http.authorizeHttpRequests(config -> config
                .requestMatchers(PathRequest.toH2Console()).permitAll()
                .requestMatchers(
                        mvcMatcher.pattern("/sign-up/**"),
                        mvcMatcher.pattern("/check/**"),
                        mvcMatcher.pattern("/login/**")).permitAll()
                .anyRequest().authenticated());
        
        http.exceptionHandling(config -> config
                .accessDeniedHandler((request, response, exception) -> {
                    // 접근할 수 없는 권한
                    throw new MemberException(EXCEPTION_PERMISSION);
                })
                .authenticationEntryPoint((request, response, exception) -> {
                    // 유효한 자격증명을 제공하지 않고 접근하려 할때
                    throw new MemberException(EXCEPTION_AUTHENTICATION);
                }));
        
        http.httpBasic(withDefaults());
        http.csrf(AbstractHttpConfigurer::disable);
        http.headers(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        

        http.addFilterBefore(exceptionHandlerFilter, SecurityContextHolderFilter.class);
        http.addFilterBefore(sessionCheckFilter, ExceptionHandlerFilter.class);
        http.addFilterAt(authenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtFilter, AuthenticationFilter.class);
        
        return http.build();
    }
    
    private AuthenticationFilter authenticationFilter() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter(memberService, jwtProvider);
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }
    
}
