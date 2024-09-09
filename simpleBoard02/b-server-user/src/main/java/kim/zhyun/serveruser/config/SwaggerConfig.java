package kim.zhyun.serveruser.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import kim.zhyun.serveruser.filter.AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static kim.zhyun.jwt.common.constants.JwtConstants.JWT_HEADER;
import static kim.zhyun.jwt.common.constants.JwtConstants.JWT_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {
    @Value("${swagger.server}")
    private String serverUrl;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${spring.security.debug}")
    private boolean enableSecurityDebugMode;

    private final ApplicationContext applicationContext;

    private final String LOGIN_API_REQUEST_BODY_USERNAME = "email";
    private final String LOGIN_API_REQUEST_BODY_PASSWORD = "password";
    private final String LOGIN_API_NAME = "1. 로그인";
    private final String LOGIN_API_DESCRIPTION = "로그인 후 인증키는 header에서 `x-token` 확인";


    public OpenApiCustomizer securityLoginEndpointCustomiser() {
        FilterChainProxy filterChainProxy = applicationContext.getBean(AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME, FilterChainProxy.class);
        return openAPI -> {
            for (SecurityFilterChain filterChain : filterChainProxy.getFilterChains()) {
                Optional<AuthenticationFilter> optionalFilter =
                        filterChain.getFilters().stream()
                                .filter(AuthenticationFilter.class::isInstance)
                                .map(AuthenticationFilter.class::cast)
                                .findAny();
                if (optionalFilter.isPresent()) {
                    AuthenticationFilter authenticationFilter = optionalFilter.get();
                    Operation operation = new Operation();
                    Schema<?> schema = new ObjectSchema()
                            .addProperty(LOGIN_API_REQUEST_BODY_USERNAME, new StringSchema())
                            .addProperty(LOGIN_API_REQUEST_BODY_PASSWORD, new StringSchema());

                    String mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
                    RequestBody requestBody = new RequestBody().required(true).content(new Content().addMediaType(mediaType, new MediaType().schema(schema)));
                    operation.requestBody(requestBody);

                    ApiResponses apiResponses = new ApiResponses();
                    apiResponses.addApiResponse(String.valueOf(HttpStatus.OK.value()),new ApiResponse().description(HttpStatus.OK.getReasonPhrase()));
                    apiResponses.addApiResponse(String.valueOf(HttpStatus.FORBIDDEN.value()), new ApiResponse().description(HttpStatus.FORBIDDEN.getReasonPhrase()));
                    operation.responses(apiResponses);

                    operation.addTagsItem(LOGIN_API_NAME);
                    operation.description(LOGIN_API_DESCRIPTION);

                    PathItem pathItem = new PathItem().post(operation);
                    try {
                        Field requestMatcherField = AbstractAuthenticationProcessingFilter.class.getDeclaredField("requiresAuthenticationRequestMatcher");
                        requestMatcherField.setAccessible(true);
                        AntPathRequestMatcher requestMatcher = (AntPathRequestMatcher) requestMatcherField.get(authenticationFilter);
                        String loginPath = requestMatcher.getPattern();
                        requestMatcherField.setAccessible(false);
                        openAPI.getPaths().addPathItem(loginPath, pathItem);
                    }
                    catch (NoSuchFieldException | IllegalAccessException |
                           ClassCastException ignored) {
                        // Exception escaped
                        log.trace(ignored.getMessage());
                    }
                }
            }
        };
    }


    @Bean
    public GroupedOpenApi signUpGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("1. 회원 가입 API")
                .pathsToMatch("/sign-up", "/check/**")
                .build();
    }

    @Bean
    public GroupedOpenApi memberGroupedOpenApi() {
        GroupedOpenApi groupedOpenApi = GroupedOpenApi.builder()
                .group("2. 회원 API")
                .pathsToExclude("/sign-up", "/check/auth", "/check/duplicate-email", "/member/all", "/member/role")
                .build();

        if (!enableSecurityDebugMode) {
            groupedOpenApi.addAllOpenApiCustomizer(Set.of(securityLoginEndpointCustomiser()));
        }

        return groupedOpenApi;
    }

    @Bean
    public GroupedOpenApi loginGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("3. 관리자 API")
                .pathsToMatch("/member/all", "/member/role")
                .build();
    }


    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .version("1.0")
                .title("Simple Board 02 - User Service")
                .description("""
        📢 인증키 입력시 다음 형태로 입력해야 한다.
         
        Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnaW13bGd1c0BnbWFpbC5jb20iLCJpZCI6MywiZX
        """);

        Server server = new Server();
        server.setUrl("%s%s".formatted(serverUrl, contextPath));

        // jwt
        String jwtKey = JWT_HEADER;
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtKey);
        Components components = new Components()
                .addSecuritySchemes(jwtKey, new SecurityScheme()
                        .name(jwtKey)
                        .type(SecurityScheme.Type.APIKEY)
                        .scheme(JWT_PREFIX)
                        .bearerFormat("JWT") // JWT, OAuth 등
                        .in(SecurityScheme.In.HEADER));

        return new OpenAPI()
                .info(info)
                .components(components)
                .addSecurityItem(securityRequirement)
                .servers(List.of(server));
    }

}