package kim.zhyun.serverarticle.config;

import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.jwt.filter.ExceptionHandlerFilter;
import kim.zhyun.jwt.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import static kim.zhyun.jwt.common.constants.type.RoleType.TYPE_ADMIN;
import static kim.zhyun.jwt.common.constants.type.RoleType.TYPE_MEMBER;
import static kim.zhyun.jwt.exception.message.ExceptionMessage.EXCEPTION_AUTHENTICATION;
import static kim.zhyun.jwt.exception.message.ExceptionMessage.EXCEPTION_PERMISSION;
import static org.springframework.http.HttpMethod.GET;

@RequiredArgsConstructor
@EnableWebSecurity(debug = true)
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {
    private final ExceptionHandlerFilter exceptionHandlerFilter;
    private final JwtFilter jwtFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http.authorizeHttpRequests(config -> config
                .requestMatchers(PathRequest.toH2Console()).permitAll()
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        
                        "/error/**",
                        
                        "/delete/withdrawal"
                ).permitAll()
                .requestMatchers(GET).permitAll()
                
                .anyRequest().hasAnyRole(TYPE_ADMIN, TYPE_MEMBER));
        
        http.exceptionHandling(config -> config
                .accessDeniedHandler((request, response, exception) -> {
                    // 접근할 수 없는 권한
                    throw new ApiException(EXCEPTION_PERMISSION);
                })
                .authenticationEntryPoint((request, response, exception) -> {
                    // 유효한 자격증명을 제공하지 않고 접근하려 할때
                    throw new ApiException(EXCEPTION_AUTHENTICATION);
                }));
        
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);
        http.headers(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);

        http.addFilterBefore(exceptionHandlerFilter, SecurityContextHolderFilter.class);
        http.addFilterAfter(jwtFilter, SecurityContextHolderFilter.class);
        
        return http.build();
    }
    
}
