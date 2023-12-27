package kim.zhyun.serveruser.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import kim.zhyun.serveruser.data.message.ResponseMessage;
import kim.zhyun.serveruser.data.type.ExceptionType;
import lombok.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static kim.zhyun.serveruser.data.type.ExceptionType.RESPONSE_API_MESSAGE_INPUT_FAULT;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(NON_NULL)
public class ApiResponse <T> {
    
    @Getter private Boolean status;
    @Getter private T result;
    
    private Object message;
    
    public String getMessage() {
        if (message instanceof ResponseMessage)
            return ((ResponseMessage) message).getMessage();
        
        if (message instanceof ExceptionType)
            return ((ExceptionType) message).getDescription();
        
        throw new RuntimeException(RESPONSE_API_MESSAGE_INPUT_FAULT.getDescription());
    }
}
