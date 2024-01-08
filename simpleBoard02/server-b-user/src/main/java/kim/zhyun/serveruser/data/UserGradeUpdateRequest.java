package kim.zhyun.serveruser.data;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import static kim.zhyun.serveruser.data.message.ExceptionMessage.EXCEPTION_VALID_FORMAT;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class UserGradeUpdateRequest {
    private long id;
    
    @NotNull(message = EXCEPTION_VALID_FORMAT)
    private String role;
}
