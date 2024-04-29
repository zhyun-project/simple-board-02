package kim.zhyun.jwt.exception.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Builder
public class ValidExceptionResponse implements Comparable<ValidExceptionResponse> {
    
    private String field;
    private String message;
    
    @Override
    public int compareTo(ValidExceptionResponse o) {
        return this.field.compareTo(o.field);
    }
    
}