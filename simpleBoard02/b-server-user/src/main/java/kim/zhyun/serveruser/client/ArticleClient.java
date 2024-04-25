package kim.zhyun.serveruser.client;

import kim.zhyun.serveruser.data.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Set;

@FeignClient(name = "articleClient", url = "http://localhost:8080/api/article")
public interface ArticleClient {
    
    @PostMapping("/delete/withdrawal")
    ResponseEntity<ApiResponse<Object>> withdrawalArticleDelete(@RequestBody Set<Long> userIds);
    
}

