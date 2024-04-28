package kim.zhyun.serverarticle.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Builder
public class ApiResponse <T> {
    
    private Boolean status;
    private String message;
    private T result;
    
}
