package kim.zhyun.serveruser.domain.member.controller.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import static kim.zhyun.jwt.exception.message.CommonExceptionMessage.EXCEPTION_VALID_FORMAT;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class UserGradeUpdateRequest {
    private long id;
    
    @NotNull(message = EXCEPTION_VALID_FORMAT)
    private String role;
}
