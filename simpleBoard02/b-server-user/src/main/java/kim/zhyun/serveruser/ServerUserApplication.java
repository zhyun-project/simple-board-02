package kim.zhyun.serveruser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"kim.zhyun.serveruser", "kim.zhyun.jwt"})
public class ServerUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerUserApplication.class, args);
    }
}
