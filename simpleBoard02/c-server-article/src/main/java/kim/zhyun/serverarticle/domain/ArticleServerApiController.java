package kim.zhyun.serverarticle.domain;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class ArticleServerApiController {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${swagger.server}")
    private String server;


    @Operation(hidden = true, description = "`swagger ui`로 리다이렉트")
    @GetMapping("/")
    public ResponseEntity<Object> gotoSwaggerUI() {
        HttpHeaders header = new HttpHeaders();
        header.setLocation(URI.create(server + contextPath + "/swagger-ui/index.html"));
        return new ResponseEntity<>(header, HttpStatus.MOVED_PERMANENTLY);
    }

}
