package kim.zhyun.serveruser.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import kim.zhyun.serveruser.data.message.ResponseMessage;
import kim.zhyun.serveruser.data.message.ExceptionMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static kim.zhyun.serveruser.data.message.ExceptionMessage.RESPONSE_API_MESSAGE_INPUT_FAULT;

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
        
        if (message instanceof ExceptionMessage)
            return ((ExceptionMessage) message).getMessage();
        
        throw new RuntimeException(RESPONSE_API_MESSAGE_INPUT_FAULT.getMessage());
    }
}
