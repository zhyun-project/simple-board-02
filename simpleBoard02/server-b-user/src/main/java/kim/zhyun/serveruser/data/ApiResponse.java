package kim.zhyun.serveruser.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import kim.zhyun.serveruser.data.type.ResponseMessage;
import lombok.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(NON_NULL)
public class ApiResponse <T> {
    
    @Getter private Boolean status;
    @Getter private T result;
    
    private ResponseMessage message;
    
    public String getMessage() {
        return message.getMessage();
    }
}
