package kim.zhyun.serveruser.config;

import kim.zhyun.jwt.filter.JwtFilter;
import kim.zhyun.jwt.provider.JwtProvider;
import kim.zhyun.serveruser.advice.security.SecurityAccessDeniedException;
import kim.zhyun.serveruser.advice.security.SecurityAuthenticationEntryPoint;
import kim.zhyun.serveruser.filter.AuthenticationFilter;
import kim.zhyun.serveruser.filter.ExceptionHandlerFilter;
import kim.zhyun.serveruser.filter.SessionCheckFilter;
import kim.zhyun.serveruser.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.springframework.security.config.Customizer.withDefaults;

@RequiredArgsConstructor
@EnableWebSecurity(debug = true)
@Configuration
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtProvider jwtProvider;
    private final MemberService userService;
    
    private final SessionCheckFilter sessionCheckFilter;
    private final ExceptionHandlerFilter exceptionHandlerFilter;
    private final JwtFilter jwtFilter;
    
    private final SecurityAccessDeniedException accessDeniedException;
    private final SecurityAuthenticationEntryPoint authenticationEntryPoint;
    
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
                .accessDeniedHandler(accessDeniedException)
                .authenticationEntryPoint(authenticationEntryPoint));
        
        http.httpBasic(withDefaults());
        http.csrf(AbstractHttpConfigurer::disable);
        http.headers(AbstractHttpConfigurer::disable);
        
        http.addFilterBefore(exceptionHandlerFilter, SecurityContextHolderFilter.class);
        http.addFilterBefore(sessionCheckFilter, ExceptionHandlerFilter.class);
        http.addFilterAt(authenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtFilter, AuthenticationFilter.class);
        
        return http.build();
    }
    
    private AuthenticationFilter authenticationFilter() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter(userService, jwtProvider);
        filter.setAuthenticationManager(authenticationConfiguration.getAuthenticationManager());
        return filter;
    }
    
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
}
