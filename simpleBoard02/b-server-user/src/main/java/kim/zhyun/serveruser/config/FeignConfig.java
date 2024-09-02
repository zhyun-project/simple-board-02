package kim.zhyun.serveruser.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.form.spring.SpringFormEncoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class FeignConfig {
    
    @Bean
    public SpringFormEncoder multipartFormEncoder() {
        return new SpringFormEncoder(new SpringEncoder(() -> new HttpMessageConverters(new RestTemplate().getMessageConverters())));
    }

    // 타임아웃 설정
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(Duration.ofSeconds(1), Duration.ofSeconds(5), false);
    }

    // 재시도 설정
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(1000, 5000, 10);
    }
    
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
}
