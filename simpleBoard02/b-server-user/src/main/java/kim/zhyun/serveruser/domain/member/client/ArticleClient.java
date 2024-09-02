package kim.zhyun.serveruser.domain.member.client;

import kim.zhyun.jwt.common.model.ApiResponse;
import kim.zhyun.serveruser.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Set;

@FeignClient(name = "articleClient", url = "${client.server}/api/article", configuration = FeignConfig.class)
public interface ArticleClient {
    
    @PostMapping("/delete/withdrawal")
    ResponseEntity<ApiResponse<Void>> withdrawalArticleDelete(@RequestBody Set<Long> userIds);
    
}

