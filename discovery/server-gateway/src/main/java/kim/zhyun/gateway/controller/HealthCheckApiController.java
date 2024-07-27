package kim.zhyun.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckApiController {

    @GetMapping("healthcheck")
    public ResponseEntity<Object> healthCheck() {
        return ResponseEntity.ok("OK");
    }

}
