package kim.zhyun.serverarticle.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
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
    public GroupedOpenApi signUpGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("1. 비회원 API")
                .pathsToMatch("/all/**", "/**/user/**")
                .build();
    }

    @Bean
    public GroupedOpenApi memberGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("2. 회원 API")
                .pathsToMatch("/save", "/update", "/delete")
                .build();
    }

    @Bean
    public GroupedOpenApi loginGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("3. 비공개 예정 API")
                .pathsToMatch("/delete/withdrawal")
                .build();
    }


    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .version("1.0")
                .title("Simple Board 02 - Article Service")
                .description("""
                        📢 인증키 입력시 다음 형태로 입력해야 한다.
                        
                        Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZ
                        """);

        Server server = new Server();
        server.setUrl("%s%s".formatted(serverUrl, contextPath));

        // jwt
        String jwtKey = "X-TOKEN";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtKey);
        Components components = new Components()
                .addSecuritySchemes(jwtKey, new SecurityScheme()
                        .name(jwtKey)
                        .type(SecurityScheme.Type.APIKEY)
                        .scheme("Bearer")
                        .bearerFormat("JWT") // JWT, OAuth 등
                        .in(SecurityScheme.In.HEADER));

        return new OpenAPI()
                .info(info)
                .servers(List.of(server))
                .addSecurityItem(securityRequirement)
                .components(components);
    }

}