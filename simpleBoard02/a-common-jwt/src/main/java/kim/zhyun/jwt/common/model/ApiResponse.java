package kim.zhyun.jwt.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse <T> {
    
    private Boolean status;
    private String message;
    private T result;
    
}
