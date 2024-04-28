package kim.zhyun.serverarticle.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class ValidExceptionResponse implements Comparable<ValidExceptionResponse> {
    
    private String field;
    private String message;
    
    @Override
    public int compareTo(ValidExceptionResponse o) {
        return this.field.compareTo(o.field);
    }
    
}