package kim.zhyun.serveruser.config;


import kim.zhyun.jwt.exception.ApiException;
import kim.zhyun.jwt.filter.ExceptionHandlerFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import static kim.zhyun.jwt.common.constants.type.RoleType.TYPE_ADMIN;
import static kim.zhyun.jwt.common.constants.type.RoleType.TYPE_MEMBER;
import static kim.zhyun.jwt.exception.message.CommonExceptionMessage.EXCEPTION_AUTHENTICATION;
import static kim.zhyun.jwt.exception.message.CommonExceptionMessage.EXCEPTION_PERMISSION;
import static org.springframework.security.config.Customizer.withDefaults;

@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
@TestConfiguration
public class TestSecurityConfig {
    private final ExceptionHandlerFilter exceptionHandlerFilter;

    @Value("${spring.security.debug:false}")
    private boolean securityDebug;


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.debug(securityDebug);
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http.authorizeHttpRequests(config -> config
                .requestMatchers(
                        "/h2/**",
                        "/h2-console/**",

                        "/",
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
        
        http.httpBasic(withDefaults());
        http.csrf(AbstractHttpConfigurer::disable);
        http.headers(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        
        http.addFilterBefore(exceptionHandlerFilter, SecurityContextHolderFilter.class);
        
        return http.build();
    }

}
