package kim.zhyun.serveruser.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Value("${swagger.server}")
    private String serverUrl;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .version("1.0")
                .title("Simple Board 02 - User API")
                .description("회원 관련 API");

        Server server = new Server();
        server.setUrl("%s%s".formatted(serverUrl, contextPath));

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }

}