package kim.zhyun.serveruser.config;

import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.jwt.filter.ExceptionHandlerFilter;
import kim.zhyun.jwt.filter.JwtFilter;
import kim.zhyun.serveruser.filter.AuthenticationFilter;
import kim.zhyun.serveruser.filter.SessionCheckFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static kim.zhyun.jwt.common.constants.type.RoleType.TYPE_ADMIN;
import static kim.zhyun.jwt.common.constants.type.RoleType.TYPE_MEMBER;
import static kim.zhyun.jwt.exception.message.CommonExceptionMessage.EXCEPTION_AUTHENTICATION;
import static kim.zhyun.jwt.exception.message.CommonExceptionMessage.EXCEPTION_PERMISSION;
import static org.springframework.security.config.Customizer.withDefaults;

@RequiredArgsConstructor
@EnableWebSecurity(debug = true)
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {
    private final AuthenticationFilter authenticationFilter;
    private final SessionCheckFilter sessionCheckFilter;
    private final ExceptionHandlerFilter exceptionHandlerFilter;
    private final JwtFilter jwtFilter;
    
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(config -> config
                .requestMatchers(
                        "/h2/**",
                        "/h2-console/**",

                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        
                        "/error/**",
                        
                        "/sign-up/**",
                        "/check/**",
                        "/login/**"
                ).permitAll()
                
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

        http.cors(config -> {
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

            CorsConfiguration setting = new CorsConfiguration();
            setting.setAllowedOrigins(List.of(
                    "http://172.19.0.0/16",
                    "http://172.20.0.0/16",
                    "http://localhost:8080",
                    "http://zhyun.kim", "https://zhyun.kim",
                    "http://www.zhyun.kim", "https://www.zhyun.kim"));
            setting.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
            setting.addAllowedMethod("*");
            setting.addAllowedHeader("*");
            setting.setAllowCredentials(true);

            source.registerCorsConfiguration("/**", setting);

            config.configurationSource(source);
        });

        http.httpBasic(withDefaults());
        http.csrf(AbstractHttpConfigurer::disable);
        http.headers(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        

        http.addFilterBefore(exceptionHandlerFilter, SecurityContextHolderFilter.class);
        http.addFilterBefore(sessionCheckFilter, ExceptionHandlerFilter.class);
        http.addFilterAt(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtFilter, AuthenticationFilter.class);
        
        return http.build();
    }
    
}
