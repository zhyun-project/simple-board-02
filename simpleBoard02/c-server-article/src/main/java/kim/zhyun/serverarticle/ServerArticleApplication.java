package kim.zhyun.serverarticle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"kim.zhyun.serverarticle", "kim.zhyun.jwt"})
public class ServerArticleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerArticleApplication.class, args);
    }
}
