package kim.zhyun.serveruser.config;

import kim.zhyun.serveruser.interceptor.ConnectionInterceptor;
import kim.zhyun.serveruser.interceptor.DisconnectionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String[] SESSION_REQUIRED_LIST = { "/sign-up/**", "/check/**" };

    private final ConnectionInterceptor connectionInterceptor;
    private final DisconnectionInterceptor disconnectionInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(connectionInterceptor)
                .addPathPatterns(SESSION_REQUIRED_LIST);
        
        registry.addInterceptor(disconnectionInterceptor)
                .excludePathPatterns(SESSION_REQUIRED_LIST);
    }
    
}
