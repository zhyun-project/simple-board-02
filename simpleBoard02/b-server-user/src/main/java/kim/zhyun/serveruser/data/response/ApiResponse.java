package kim.zhyun.serveruser.data.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Builder
@JsonInclude(NON_NULL)
public class ApiResponse <T> {
    
    private Boolean status;
    private String message;
    private T result;
    
}
